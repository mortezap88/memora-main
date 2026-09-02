package com.example.data.speech

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.remote.ContentItem
import com.example.data.remote.GeminiClient
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.GenerationConfig
import com.example.data.remote.InlineData
import com.example.data.remote.PartItem
import com.example.data.remote.ThinkingConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

sealed interface AudioRecordState {
    object Idle : AudioRecordState
    data class Recording(val durationSeconds: Int, val amplitude: Float) : AudioRecordState
    object Transcribing : AudioRecordState
    data class Success(val transcribedText: String) : AudioRecordState
    data class Error(val message: String) : AudioRecordState
}

class AudioRecorderManager(private val context: Context) {
    private val TAG = "AudioRecorderManager"
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow<AudioRecordState>(AudioRecordState.Idle)
    val state: StateFlow<AudioRecordState> = _state.asStateFlow()

    private val _durationSeconds = MutableStateFlow(0)
    val durationSeconds: StateFlow<Int> = _durationSeconds.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    fun isRecording(): Boolean = _state.value is AudioRecordState.Recording

    fun startRecording(): Boolean {
        stopRecordingInternal(discard = true)
        _durationSeconds.value = 0
        _amplitude.value = 0f

        // Pre-warm HTTP/2 network connection in parallel while user speaks
        GeminiClient.prewarm()

        return try {
            val file = File(context.cacheDir, "user_voice_message_${System.currentTimeMillis()}.m4a")
            if (file.exists()) file.delete()
            audioFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioChannels(1)
                setAudioSamplingRate(16000)
                setAudioEncodingBitRate(32000)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder

            _state.value = AudioRecordState.Recording(0, 0f)

            // Polling job for amplitude and duration - continuous, will NOT stop on silence
            recordingJob = scope.launch(Dispatchers.Default) {
                var seconds = 0
                var counter = 0
                while (isActive && mediaRecorder != null) {
                    delay(100)
                    counter++
                    if (counter % 10 == 0) {
                        seconds++
                        _durationSeconds.value = seconds
                    }
                    val maxAmp = try {
                        mediaRecorder?.maxAmplitude ?: 0
                    } catch (e: Exception) {
                        0
                    }
                    val normalized = (maxAmp / 32767f).coerceIn(0f, 1f)
                    _amplitude.value = normalized
                    _state.value = AudioRecordState.Recording(seconds, normalized)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio recording", e)
            _state.value = AudioRecordState.Error("Could not start microphone: ${e.localizedMessage ?: "Unknown error"}")
            false
        }
    }

    fun stopAndTranscribe(
        selectedModel: String = "gemini-flash-latest",
        customApiKey: String = "",
        backupText: String = "",
        onResult: (String) -> Unit
    ) {
        val file = audioFile
        stopRecordingInternal(discard = false)

        _state.value = AudioRecordState.Transcribing

        scope.launch(Dispatchers.IO) {
            // Guarantee file system buffer flush before reading bytes
            delay(60)

            if (file == null || !file.exists() || file.length() < 150) {
                withContext(Dispatchers.Main) {
                    if (backupText.isNotBlank()) {
                        _state.value = AudioRecordState.Success(backupText)
                        onResult(backupText)
                    } else {
                        _state.value = AudioRecordState.Error("Audio recording was too short. Please try again.")
                    }
                }
                return@launch
            }

            try {
                val bytes = file.readBytes()
                val base64Audio = Base64.encodeToString(bytes, Base64.NO_WRAP)
                val apiKey = customApiKey.trim().ifBlank { BuildConfig.GEMINI_API_KEY }

                if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                    val request = GenerateContentRequest(
                        contents = listOf(
                            ContentItem(
                                role = "user",
                                parts = listOf(
                                    PartItem(
                                        inlineData = InlineData(
                                            mimeType = "audio/mp4",
                                            data = base64Audio
                                        )
                                    ),
                                    PartItem(
                                        text = "Transcribe the spoken words in this audio into clean English text. Return ONLY the transcribed words with proper punctuation. Do not include notes or conversational responses."
                                    )
                                )
                            )
                        ),
                        generationConfig = GenerationConfig(
                            temperature = 0.0f,
                            topP = 0.9f
                        )
                    )

                    // Multimodal Gemini endpoints prioritized starting with the user's selected model
                    val cleanSelected = selectedModel.trim().ifBlank { "gemini-flash-latest" }
                    val modelsToTry = mutableListOf<String>().apply {
                        add(cleanSelected)
                        val fallbacks = listOf(
                            "gemini-2.5-flash-lite",
                            "gemini-2.5-flash",
                            "gemini-flash-latest",
                            "gemini-2.5-pro",
                            "gemini-1.5-flash"
                        )
                        for (f in fallbacks) {
                            if (f != cleanSelected && !contains(f)) {
                                add(f)
                            }
                        }
                    }
                    var transcribedResult: String? = null
                    var errorOccurred: Exception? = null

                    for (model in modelsToTry) {
                        try {
                            val response = GeminiClient.api.generateContent(
                                model = model,
                                apiKey = apiKey,
                                request = request
                            )
                            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                            if (!rawText.isNullOrBlank()) {
                                // Clean up any wrapping quotes if returned
                                val cleanText = rawText.removeSurrounding("\"").removeSurrounding("'").trim()
                                transcribedResult = cleanText
                                break
                            }
                        } catch (e: Exception) {
                            errorOccurred = e
                            if (e.message?.contains("403") == true || e.message?.contains("401") == true) {
                                break
                            }
                        }
                    }

                    if (!transcribedResult.isNullOrBlank()) {
                        withContext(Dispatchers.Main) {
                            _state.value = AudioRecordState.Success(transcribedResult)
                            onResult(transcribedResult)
                        }
                        file.delete()
                        return@launch
                    }

                    // If API returned error but we have backup text
                    if (backupText.isNotBlank()) {
                        withContext(Dispatchers.Main) {
                            _state.value = AudioRecordState.Success(backupText)
                            onResult(backupText)
                        }
                        file.delete()
                        return@launch
                    }

                    val errMsg = when {
                        errorOccurred?.message?.contains("403") == true -> "HTTP 403 Forbidden. Please check your Gemini API key in Settings."
                        else -> errorOccurred?.localizedMessage ?: "Transcription failed"
                    }
                    withContext(Dispatchers.Main) {
                        _state.value = AudioRecordState.Error("Voice transcription error: $errMsg")
                    }
                    file.delete()
                    return@launch
                }

                // If API key is not configured
                if (backupText.isNotBlank()) {
                    withContext(Dispatchers.Main) {
                        _state.value = AudioRecordState.Success(backupText)
                        onResult(backupText)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _state.value = AudioRecordState.Error("Audio captured. Enter your Gemini API key in Settings for AI voice transcription.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Audio transcription failed", e)
                if (backupText.isNotBlank()) {
                    withContext(Dispatchers.Main) {
                        _state.value = AudioRecordState.Success(backupText)
                        onResult(backupText)
                    }
                } else {
                    val errMsg = if (e.message?.contains("403") == true) "HTTP 403 Forbidden. Check your Gemini API key in Settings." else (e.localizedMessage ?: "Unknown error")
                    withContext(Dispatchers.Main) {
                        _state.value = AudioRecordState.Error("Voice transcription error: $errMsg")
                    }
                }
            } finally {
                file.delete()
            }
        }
    }

    fun cancelRecording() {
        stopRecordingInternal(discard = true)
        _state.value = AudioRecordState.Idle
        _durationSeconds.value = 0
        _amplitude.value = 0f
    }

    fun resetState() {
        _state.value = AudioRecordState.Idle
        _durationSeconds.value = 0
        _amplitude.value = 0f
    }

    private fun stopRecordingInternal(discard: Boolean) {
        recordingJob?.cancel()
        recordingJob = null
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            // Already stopped or released
        } finally {
            mediaRecorder = null
        }
        if (discard) {
            audioFile?.delete()
            audioFile = null
        }
    }

    fun shutdown() {
        stopRecordingInternal(discard = true)
    }
}
