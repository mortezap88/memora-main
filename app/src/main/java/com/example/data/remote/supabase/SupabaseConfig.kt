package com.example.data.remote.supabase

object SupabaseConfig {
    const val DEFAULT_SUPABASE_URL = "https://zfawvopcncteiwmpghyg.supabase.co"
    const val DEFAULT_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpmYXd2b3BjbmN0ZWl3bXBnaHlnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODgzNTE1NTAsImV4cCI6MjEwMzkyNzU1MH0.8dIh64GUY7eSBgMSjIKKwTKHuGgFfz2169iS3C_RQYo"

    val REST_BASE_URL: String
        get() = "$DEFAULT_SUPABASE_URL/rest/v1"
}

enum class CloudSyncState {
    SYNCED,
    SYNCING,
    OFFLINE,
    ERROR
}
