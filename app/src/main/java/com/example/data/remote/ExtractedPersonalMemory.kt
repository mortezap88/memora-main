package com.example.data.remote

data class ExtractedPersonalMemory(
    val keyword: String, // e.g. "morteza", "ali", "pizza"
    val displayName: String, // e.g. "Morteza", "Ali"
    val factsSummary: String // e.g. "User's close friend; ate pizza with user and talked about study plans."
)
