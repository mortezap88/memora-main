package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.NotepadEntity
import com.example.data.remote.ImageSearchService
import com.example.data.remote.SearchImageResult
import com.example.ui.theme.CoachPurple
import kotlinx.coroutines.launch

enum class ImageBrowserMode {
    IMAGE_SEARCH,
    WEB_BROWSER
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ImageBrowserScreen(
    isOpen: Boolean,
    onClose: () -> Unit,
    notepads: List<NotepadEntity> = emptyList(),
    selectedNotepadId: String? = null,
    onInsertToNotepad: ((notepadId: String, imageUrl: String, title: String) -> Unit)? = null,
    onCreateCardWithImage: ((imageUrl: String, title: String) -> Unit)? = null,
    onSelectImage: ((imageUrl: String) -> Unit)? = null,
    initialQuery: String = "biology anatomy"
) {
    if (!isOpen) return

    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    var browserMode by remember { mutableStateOf(ImageBrowserMode.IMAGE_SEARCH) }
    var searchQuery by remember { mutableStateOf(initialQuery) }
    var webUrl by remember { mutableStateOf("https://commons.wikimedia.org/wiki/Main_Page") }
    var webTitle by remember { mutableStateOf("Wikimedia Commons") }
    var webProgress by remember { mutableIntStateOf(0) }
    var isWebLoading by remember { mutableStateOf(false) }

    // Search Results State
    var searchResults by remember { mutableStateOf<List<SearchImageResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }

    // Extracted Images from Web Page State
    var extractedWebImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var showExtractedSheet by remember { mutableStateOf(false) }

    // Fullscreen Inspector / Preview State
    var inspectingImage by remember { mutableStateOf<SearchImageResult?>(null) }
    var copyToastVisible by remember { mutableStateOf(false) }
    var insertSuccessToast by remember { mutableStateOf<String?>(null) }

    // WebView reference
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Suggested search topics
    val suggestedTopics = remember {
        listOf(
            "Brain Anatomy",
            "Cell Biology",
            "Solar System",
            "World Map",
            "Tokyo",
            "Architecture",
            "Nature & Forest",
            "Abstract Art",
            "Human Heart",
            "Microbiology",
            "Ancient Egypt",
            "Neural Network"
        )
    }

    // Execute Image Search function
    fun performSearch(query: String) {
        if (query.isBlank()) return
        scope.launch {
            isSearching = true
            searchError = null
            keyboardController?.hide()
            try {
                val results = ImageSearchService.searchImages(query, limit = 36)
                searchResults = results
                if (results.isEmpty()) {
                    searchError = "No images found for \"$query\". Try a different keyword."
                }
            } catch (e: Exception) {
                searchError = "Error loading images: ${e.localizedMessage}"
            } finally {
                isSearching = false
            }
        }
    }

    // Initial search
    LaunchedEffect(Unit) {
        if (searchResults.isEmpty() && searchQuery.isNotBlank()) {
            performSearch(searchQuery)
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            color = if (isDark) Color(0xFF0C0E17) else Color(0xFFF8FAFC),
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .testTag("image_browser_dialog")
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                // TOP HEADER BAR: Mode Selector & Close Button
                Surface(
                    color = if (isDark) Color(0xFF141624) else Color(0xFFFFFFFF),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0)),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = CoachPurple.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "Image Browser",
                                        tint = CoachPurple,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Image Browser",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = if (isDark) Color.White else Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = if (browserMode == ImageBrowserMode.IMAGE_SEARCH) "Search & find high-res images" else "Browse web pages & extract images",
                                        fontSize = 11.5.sp,
                                        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                                    )
                                }
                            }

                            // Close Button
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F5F9))
                                    .testTag("close_image_browser")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Browser",
                                    tint = if (isDark) Color.White else Color(0xFF334155),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // TAB SWITCHER: [Image Search] | [Web Browser]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFF1F5F9))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (browserMode == ImageBrowserMode.IMAGE_SEARCH) CoachPurple else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { browserMode = ImageBrowserMode.IMAGE_SEARCH }
                                    .testTag("tab_image_search")
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = if (browserMode == ImageBrowserMode.IMAGE_SEARCH) Color.White else (if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569)),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Image Search",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = if (browserMode == ImageBrowserMode.IMAGE_SEARCH) Color.White else (if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569))
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (browserMode == ImageBrowserMode.WEB_BROWSER) CoachPurple else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { browserMode = ImageBrowserMode.WEB_BROWSER }
                                    .testTag("tab_web_browser")
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = if (browserMode == ImageBrowserMode.WEB_BROWSER) Color.White else (if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569)),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Web Browser",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = if (browserMode == ImageBrowserMode.WEB_BROWSER) Color.White else (if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF475569))
                                    )
                                }
                            }
                        }
                    }
                }

                // BODY CONTENT DEPENDING ON MODE
                if (browserMode == ImageBrowserMode.IMAGE_SEARCH) {
                    // MODE 1: VISUAL IMAGE SEARCH
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Search Bar Card
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isDark) Color(0xFF141624) else Color(0xFFFFFFFF),
                            border = BorderStroke(1.2.dp, if (isDark) Color.White.copy(alpha = 0.16f) else Color(0xFFE2E8F0)),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = CoachPurple,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    textStyle = TextStyle(
                                        color = if (isDark) Color.White else Color(0xFF0F172A),
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(CoachPurple),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { performSearch(searchQuery) }),
                                    decorationBox = { innerTextField ->
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search images (e.g. brain anatomy, tokyo, art...)",
                                                color = if (isDark) Color.White.copy(alpha = 0.4f) else Color(0xFF94A3B8),
                                                fontSize = 14.sp
                                            )
                                        }
                                        innerTextField()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("image_search_input")
                                )

                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF94A3B8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = CoachPurple,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { performSearch(searchQuery) }
                                        .testTag("submit_image_search")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    ) {
                                        Text(
                                            text = "Search",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        // SUGGESTED TOPIC PILLS
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            suggestedTopics.forEach { topic ->
                                val isSelected = searchQuery.equals(topic, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        searchQuery = topic
                                        performSearch(topic)
                                    },
                                    label = {
                                        Text(
                                            text = topic,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CoachPurple.copy(alpha = 0.2f),
                                        selectedLabelColor = CoachPurple,
                                        containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9),
                                        labelColor = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF475569)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isSelected) CoachPurple else (if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE2E8F0))
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.height(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // MAIN IMAGE RESULTS GRID
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                        ) {
                            if (isSearching) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    CircularProgressIndicator(
                                        color = CoachPurple,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Finding high-resolution images...",
                                        fontSize = 13.5.sp,
                                        color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B)
                                    )
                                }
                            } else if (searchError != null && searchResults.isEmpty()) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize().padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Explore,
                                        contentDescription = null,
                                        tint = if (isDark) Color.White.copy(alpha = 0.3f) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = searchError ?: "No images found",
                                        fontSize = 14.sp,
                                        color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B)
                                    )
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(top = 4.dp, bottom = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(searchResults, key = { it.id }) { image ->
                                        ImageCardItem(
                                            image = image,
                                            isDark = isDark,
                                            onClick = { inspectingImage = image },
                                            onCopy = {
                                                clipboardManager.setText(AnnotatedString(image.fullUrl))
                                                copyToastVisible = true
                                                scope.launch {
                                                    kotlinx.coroutines.delay(2000)
                                                    copyToastVisible = false
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // MODE 2: INTERACTIVE IN-APP WEB BROWSER
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Browser Address Bar
                        Surface(
                            color = if (isDark) Color(0xFF141624) else Color(0xFFFFFFFF),
                            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Nav Buttons
                                    IconButton(
                                        onClick = {
                                            if (webViewInstance?.canGoBack() == true) {
                                                webViewInstance?.goBack()
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = if (isDark) Color.White else Color(0xFF334155),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (webViewInstance?.canGoForward() == true) {
                                                webViewInstance?.goForward()
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Forward",
                                            tint = if (isDark) Color.White else Color(0xFF334155),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { webViewInstance?.reload() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Reload",
                                            tint = if (isDark) Color.White else Color(0xFF334155),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Address Text Box
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F5F9),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 8.dp)
                                        ) {
                                            BasicTextField(
                                                value = webUrl,
                                                onValueChange = { webUrl = it },
                                                singleLine = true,
                                                textStyle = TextStyle(
                                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                                    fontSize = 12.5.sp
                                                ),
                                                cursorBrush = SolidColor(CoachPurple),
                                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                                keyboardActions = KeyboardActions(onGo = {
                                                    var target = webUrl.trim()
                                                    if (!target.startsWith("http://") && !target.startsWith("https://")) {
                                                        target = if (target.contains(".")) "https://$target" else "https://www.google.com/search?tbm=isch&q=${target.replace(" ", "+")}"
                                                    }
                                                    webUrl = target
                                                    webViewInstance?.loadUrl(target)
                                                    keyboardController?.hide()
                                                }),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                // Quick Search Engine Preset Chips
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val presets = listOf(
                                        "Google Images" to "https://images.google.com",
                                        "Wikimedia" to "https://commons.wikimedia.org",
                                        "Unsplash" to "https://unsplash.com",
                                        "DuckDuckGo" to "https://duckduckgo.com/?iax=images&ia=images",
                                        "Pexels" to "https://www.pexels.com"
                                    )

                                    presets.forEach { (name, url) ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F5F9),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    webUrl = url
                                                    webViewInstance?.loadUrl(url)
                                                }
                                        ) {
                                            Text(
                                                text = name,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = CoachPurple,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Progress Bar
                        if (isWebLoading && webProgress < 100) {
                            LinearProgressIndicator(
                                progress = { webProgress / 100f },
                                color = CoachPurple,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.5.dp)
                            )
                        }

                        // Web View Container
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        settings.loadWithOverviewMode = true
                                        settings.useWideViewPort = true
                                        settings.builtInZoomControls = true
                                        settings.displayZoomControls = false

                                        addJavascriptInterface(
                                            object {
                                                @JavascriptInterface
                                                fun onImagesExtracted(urlsJson: String) {
                                                    val raw = urlsJson.replace("[", "").replace("]", "").replace("\"", "")
                                                    val list = raw.split(",").map { it.trim() }.filter {
                                                        it.startsWith("http") && (it.contains(".jpg") || it.contains(".png") || it.contains(".jpeg") || it.contains(".webp") || it.contains("unsplash") || it.contains("wikimedia") || it.contains("encrypted-tbn"))
                                                    }.distinct()
                                                    extractedWebImages = list
                                                    if (list.isNotEmpty()) {
                                                        showExtractedSheet = true
                                                    }
                                                }
                                            },
                                            "AndroidImageExtractor"
                                        )

                                        webViewClient = object : WebViewClient() {
                                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                                request?.url?.toString()?.let { url ->
                                                    webUrl = url
                                                }
                                                return false
                                            }

                                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                                super.onPageStarted(view, url, favicon)
                                                isWebLoading = true
                                                url?.let { webUrl = it }
                                            }

                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                super.onPageFinished(view, url)
                                                isWebLoading = false
                                                view?.title?.let { webTitle = it }
                                                // Scan images
                                                view?.evaluateJavascript(
                                                    """
                                                    (function() {
                                                        var imgs = document.getElementsByTagName('img');
                                                        var urls = [];
                                                        for (var i = 0; i < imgs.length; i++) {
                                                            var src = imgs[i].src || imgs[i].getAttribute('data-src');
                                                            if (src && src.startsWith('http')) {
                                                                urls.push(src);
                                                            }
                                                        }
                                                        window.AndroidImageExtractor.onImagesExtracted(JSON.stringify(urls));
                                                    })();
                                                    """.trimIndent(),
                                                    null
                                                )
                                            }
                                        }

                                        webChromeClient = object : WebChromeClient() {
                                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                                webProgress = newProgress
                                                if (newProgress >= 100) isWebLoading = false
                                            }
                                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                                title?.let { webTitle = it }
                                            }
                                        }

                                        loadUrl(webUrl)
                                        webViewInstance = this
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Floating "Extract Images from Page" Button
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = CoachPurple,
                                shadowElevation = 8.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        webViewInstance?.evaluateJavascript(
                                            """
                                            (function() {
                                                var imgs = document.getElementsByTagName('img');
                                                var urls = [];
                                                for (var i = 0; i < imgs.length; i++) {
                                                    var src = imgs[i].src || imgs[i].getAttribute('data-src');
                                                    if (src && src.startsWith('http')) {
                                                        urls.push(src);
                                                    }
                                                }
                                                window.AndroidImageExtractor.onImagesExtracted(JSON.stringify(urls));
                                            })();
                                            """.trimIndent(),
                                            null
                                        )
                                        showExtractedSheet = true
                                    }
                                    .testTag("extract_page_images_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (extractedWebImages.isNotEmpty()) "Found ${extractedWebImages.size} Images" else "Find Page Images",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // EXTRACTED WEB IMAGES DRAWER / POPUP
            if (showExtractedSheet) {
                Dialog(onDismissRequest = { showExtractedSheet = false }) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = if (isDark) Color(0xFF141624) else Color.White,
                        border = BorderStroke(1.2.dp, if (isDark) Color.White.copy(alpha = 0.16f) else Color(0xFFE2E8F0)),
                        shadowElevation = 12.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(520.dp)
                            .padding(8.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = CoachPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Page Images (${extractedWebImages.size})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (isDark) Color.White else Color(0xFF0F172A)
                                    )
                                }

                                IconButton(
                                    onClick = { showExtractedSheet = false },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = if (isDark) Color.White else Color(0xFF475569)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (extractedWebImages.isEmpty()) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Scanning page for images... Navigate or search on the web page to detect photos.",
                                        fontSize = 13.sp,
                                        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                                    )
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                ) {
                                    items(extractedWebImages) { imgUrl ->
                                        val resultItem = SearchImageResult(
                                            id = "web_${imgUrl.hashCode()}",
                                            title = webTitle,
                                            thumbnailUrl = imgUrl,
                                            fullUrl = imgUrl,
                                            source = "Webpage"
                                        )
                                        ImageCardItem(
                                            image = resultItem,
                                            isDark = isDark,
                                            onClick = {
                                                showExtractedSheet = false
                                                inspectingImage = resultItem
                                            },
                                            onCopy = {
                                                clipboardManager.setText(AnnotatedString(imgUrl))
                                                copyToastVisible = true
                                                scope.launch {
                                                    kotlinx.coroutines.delay(2000)
                                                    copyToastVisible = false
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // FULLSCREEN IMAGE INSPECTOR MODAL
            if (inspectingImage != null) {
                val image = inspectingImage!!
                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }
                val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
                    scale = (scale * zoomChange).coerceIn(1f, 5f)
                    offset += offsetChange
                }
                var showNotepadMenu by remember { mutableStateOf(false) }

                Dialog(
                    onDismissRequest = { inspectingImage = null },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.94f),
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Top Bar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = image.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${image.source} • ${if (image.width > 0) "${image.width}x${image.height}" else "High-Res"}",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.5.sp
                                    )
                                }

                                IconButton(
                                    onClick = { inspectingImage = null },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close Inspector",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Center Image Viewer with Pinch-To-Zoom
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .transformable(state = transformState)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(image.fullUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = image.title,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(
                                            scaleX = scale,
                                            scaleY = scale,
                                            translationX = offset.x,
                                            translationY = offset.y
                                        )
                                )
                            }

                            // Bottom Action Strip
                            Surface(
                                color = Color(0xFF141624),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // 1. Copy Link
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color.White.copy(alpha = 0.1f),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    clipboardManager.setText(AnnotatedString(image.fullUrl))
                                                    copyToastVisible = true
                                                    scope.launch {
                                                        kotlinx.coroutines.delay(2000)
                                                        copyToastVisible = false
                                                    }
                                                }
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(vertical = 10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Copy URL",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        // 2. Insert to Notepad
                                        Box(modifier = Modifier.weight(1.3f)) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color(0xFF2563EB),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        if (notepads.size > 1) {
                                                            showNotepadMenu = true
                                                        } else {
                                                            val targetPadId = selectedNotepadId ?: notepads.firstOrNull()?.id ?: "default"
                                                            onInsertToNotepad?.invoke(targetPadId, image.fullUrl, image.title)
                                                            insertSuccessToast = "Inserted image into Notepad!"
                                                            scope.launch {
                                                                kotlinx.coroutines.delay(2000)
                                                                insertSuccessToast = null
                                                                inspectingImage = null
                                                            }
                                                        }
                                                    }
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(vertical = 10.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.EditNote,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Insert in Note",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }

                                            DropdownMenu(
                                                expanded = showNotepadMenu,
                                                onDismissRequest = { showNotepadMenu = false }
                                            ) {
                                                notepads.forEach { pad ->
                                                    DropdownMenuItem(
                                                        text = { Text("Insert into: ${pad.title}") },
                                                        onClick = {
                                                            showNotepadMenu = false
                                                            onInsertToNotepad?.invoke(pad.id, image.fullUrl, image.title)
                                                            insertSuccessToast = "Inserted into ${pad.title}!"
                                                            scope.launch {
                                                                kotlinx.coroutines.delay(2000)
                                                                insertSuccessToast = null
                                                                inspectingImage = null
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // 3. Create Flashcard with this image
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = CoachPurple,
                                            modifier = Modifier
                                                .weight(1.3f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    onCreateCardWithImage?.invoke(image.fullUrl, image.title)
                                                    onSelectImage?.invoke(image.fullUrl)
                                                    inspectingImage = null
                                                    onClose()
                                                }
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(vertical = 10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Style,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Add to Card",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TOAST FEEDBACK OVERLAYS
            AnimatedVisibility(
                visible = copyToastVisible || insertSuccessToast != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 30.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF10B981),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = insertSuccessToast ?: "Image link copied to clipboard!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
fun ImageCardItem(
    image: SearchImageResult,
    isDark: Boolean,
    onClick: () -> Unit,
    onCopy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0xFF141624) else Color(0xFFFFFFFF),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0)),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.25f)
                    .background(if (isDark) Color(0xFF1E2235) else Color(0xFFF1F5F9))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(image.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = image.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Source Pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = image.source,
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Quick Copy Button
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Link",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Caption
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = image.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
