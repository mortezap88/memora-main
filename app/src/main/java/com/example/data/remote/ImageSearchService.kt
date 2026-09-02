package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class SearchImageResult(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val fullUrl: String,
    val width: Int = 800,
    val height: Int = 600,
    val source: String = "Web",
    val description: String = ""
)

object ImageSearchService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    const val BROWSER_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

    suspend fun searchImages(query: String, limit: Int = 20): List<SearchImageResult> = findClosestImages(query, limit)

    suspend fun findClosestImages(query: String, limit: Int = 10): List<SearchImageResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()

        val results = mutableListOf<SearchImageResult>()

        // 1. Google Image Search (Searches across all websites on the internet)
        try {
            val googleImages = fetchGoogleImages(trimmed, limit)
            for (g in googleImages) {
                if (results.none { it.thumbnailUrl == g.thumbnailUrl || it.fullUrl == g.fullUrl }) {
                    results.add(g)
                }
            }
        } catch (_: Exception) {}

        // 2. DuckDuckGo Global Web Images (Full web index spanning all websites)
        if (results.size < limit) {
            try {
                val ddgImages = fetchDuckDuckGoImages(trimmed, limit)
                for (d in ddgImages) {
                    if (results.none { it.thumbnailUrl == d.thumbnailUrl || it.fullUrl == d.fullUrl }) {
                        results.add(d)
                    }
                }
            } catch (_: Exception) {}
        }

        // 3. Wikipedia PageImages API with exact query
        if (results.size < limit) {
            try {
                val wikiPageImages = fetchWikipediaPageImages(trimmed, limit)
                for (w in wikiPageImages) {
                    if (results.none { it.thumbnailUrl == w.thumbnailUrl || it.fullUrl == w.fullUrl }) {
                        results.add(w)
                    }
                }
            } catch (_: Exception) {}
        }

        // 4. Wikipedia Summary API
        if (results.size < limit) {
            try {
                val wikiSummary = fetchWikipediaSummaryImage(trimmed)
                if (wikiSummary != null && results.none { it.thumbnailUrl == wikiSummary.thumbnailUrl }) {
                    results.add(0, wikiSummary)
                }
            } catch (_: Exception) {}
        }

        // 5. Wikimedia Commons search
        if (results.size < limit) {
            try {
                val wikiCommons = fetchWikimediaCommonsImages(trimmed, limit)
                for (c in wikiCommons) {
                    if (results.none { it.fullUrl == c.fullUrl || it.thumbnailUrl == c.thumbnailUrl }) {
                        results.add(c)
                    }
                }
            } catch (_: Exception) {}
        }

        // 6. Tokenized fallback if compound query returned too few results
        if (results.size < limit && trimmed.contains(" ")) {
            val tokens = trimmed.split(" ").filter { it.length > 2 }
            for (token in tokens) {
                if (results.size >= limit) break
                try {
                    val subGoogle = fetchGoogleImages(token, 4)
                    for (sg in subGoogle) {
                        if (results.none { it.fullUrl == sg.fullUrl || it.thumbnailUrl == sg.thumbnailUrl }) {
                            results.add(sg)
                        }
                    }
                } catch (_: Exception) {}
            }
        }

        results.filter { it.thumbnailUrl.isNotBlank() }
            .distinctBy { it.thumbnailUrl }
            .take(limit)
    }

    /**
     * Searches Google Images to pull live photographic & web results from all websites.
     */
    private fun fetchGoogleImages(term: String, limit: Int): List<SearchImageResult> {
        val list = mutableListOf<SearchImageResult>()
        val encoded = URLEncoder.encode(term, "UTF-8")
        // Use Google's dedicated image search URL
        val url = "https://www.google.com/search?q=$encoded&udm=2&hl=en&gl=us&safe=active"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", BROWSER_USER_AGENT)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()
        val html = response.body?.string() ?: return emptyList()

        // 1. Extract Google encrypted thumbnail URLs and associated metadata
        val gstaticPattern = Regex("""(https://encrypted-tbn0\.gstatic\.com/images\?q=[^"&]+&amp;s|https://encrypted-tbn0\.gstatic\.com/images\?q=[^"&]+)""")
        val gstaticMatches = gstaticPattern.findAll(html).map { it.value.replace("&amp;", "&") }.distinct().toList()

        // 2. Extract full image URLs if embedded in json callbacks
        val fullUrlPattern = Regex("""\["(https?://[^"\[\]\s]+\.(?:jpg|jpeg|png|webp|avif)(?:\?[^"\[\]\s]*)?)",\s*(\d+),\s*(\d+)\]""")
        val fullUrlMatches = fullUrlPattern.findAll(html).toList()

        // 3. Extract title/alt strings
        val altPattern = Regex("""alt="([^"]{3,100})"""")
        val altTitles = altPattern.findAll(html).map { it.groupValues[1] }.filter { !it.contains("Google") && !it.contains("logo") }.toList()

        for (i in 0 until minOf(gstaticMatches.size, limit)) {
            val thumb = gstaticMatches[i]
            val fullMatch = if (i < fullUrlMatches.size) fullUrlMatches[i] else null
            val fullUrl = fullMatch?.groupValues?.get(1) ?: thumb
            val width = fullMatch?.groupValues?.get(2)?.toIntOrNull() ?: 800
            val height = fullMatch?.groupValues?.get(3)?.toIntOrNull() ?: 600
            val title = if (i < altTitles.size) altTitles[i] else "$term image ${i + 1}"

            list.add(
                SearchImageResult(
                    id = "google_${term.hashCode()}_$i",
                    title = title,
                    thumbnailUrl = thumb,
                    fullUrl = fullUrl,
                    width = width,
                    height = height,
                    source = "Google Images",
                    description = title
                )
            )
        }
        return list
    }

    /**
     * Searches DuckDuckGo live image engine (Indexes all websites worldwide).
     */
    private fun fetchDuckDuckGoImages(term: String, limit: Int): List<SearchImageResult> {
        val list = mutableListOf<SearchImageResult>()
        val encoded = URLEncoder.encode(term, "UTF-8")

        // Step 1: Fetch vqd token from DuckDuckGo
        val searchPageUrl = "https://duckduckgo.com/?q=$encoded&iax=images&ia=images"
        val req1 = Request.Builder()
            .url(searchPageUrl)
            .header("User-Agent", BROWSER_USER_AGENT)
            .build()
        val res1 = client.newCall(req1).execute()
        if (!res1.isSuccessful) return emptyList()
        val html = res1.body?.string() ?: return emptyList()

        val vqdRegex = Regex("""vqd=([\d-]+)""")
        val vqdRegex2 = Regex("""vqd="([^"]+)"""")
        val vqdRegex3 = Regex("""vqd: '([^']+)'""")
        val vqd = vqdRegex.find(html)?.groupValues?.get(1)
            ?: vqdRegex2.find(html)?.groupValues?.get(1)
            ?: vqdRegex3.find(html)?.groupValues?.get(1)
            ?: return emptyList()

        // Step 2: Query image JSON API
        val apiUrl = "https://duckduckgo.com/i.js?l=us-en&o=json&q=$encoded&vqd=$vqd&f=,,,;&p=1"
        val req2 = Request.Builder()
            .url(apiUrl)
            .header("User-Agent", BROWSER_USER_AGENT)
            .header("Referer", "https://duckduckgo.com/")
            .header("Accept", "application/json, text/javascript, */*; q=0.01")
            .build()

        val res2 = client.newCall(req2).execute()
        if (!res2.isSuccessful) return emptyList()
        val jsonStr = res2.body?.string() ?: return emptyList()

        val root = JSONObject(jsonStr)
        val resultsArray = root.optJSONArray("results") ?: return emptyList()

        for (i in 0 until minOf(resultsArray.length(), limit)) {
            val item = resultsArray.optJSONObject(i) ?: continue
            val imgUrl = item.optString("image", "")
            val thumbUrl = item.optString("thumbnail", "").ifEmpty { imgUrl }
            val title = item.optString("title", term)
            val source = item.optString("source", "Web")
            val w = item.optInt("width", 800)
            val h = item.optInt("height", 600)

            if (thumbUrl.isNotBlank()) {
                list.add(
                    SearchImageResult(
                        id = "web_${term.hashCode()}_$i",
                        title = title,
                        thumbnailUrl = thumbUrl,
                        fullUrl = if (imgUrl.isNotBlank()) imgUrl else thumbUrl,
                        width = w,
                        height = h,
                        source = if (source.isNotBlank()) source else "Web",
                        description = title
                    )
                )
            }
        }
        return list
    }

    private fun fetchWikipediaPageImages(term: String, limit: Int): List<SearchImageResult> {
        val list = mutableListOf<SearchImageResult>()
        val encoded = URLEncoder.encode(term, "UTF-8")
        val url = "https://en.wikipedia.org/w/api.php?action=query&generator=search&gsrsearch=$encoded&gsrlimit=$limit&prop=pageimages|extracts&piprop=thumbnail|original&pithumbsize=1000&exintro=1&explaintext=1&exchars=150&format=json&origin=*"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", BROWSER_USER_AGENT)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()
        val jsonStr = response.body?.string() ?: return emptyList()
        val root = JSONObject(jsonStr)
        val queryObj = root.optJSONObject("query")
        val pagesObj = queryObj?.optJSONObject("pages") ?: return emptyList()

        val keys = pagesObj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val page = pagesObj.optJSONObject(key) ?: continue
            val thumbnailObj = page.optJSONObject("thumbnail")
            val originalObj = page.optJSONObject("original")

            val thumbUrl = thumbnailObj?.optString("source") ?: originalObj?.optString("source") ?: continue
            val fullUrl = originalObj?.optString("source") ?: thumbUrl
            val title = page.optString("title", term)
            val extract = page.optString("extract", "")

            if (thumbUrl.isNotBlank()) {
                list.add(
                    SearchImageResult(
                        id = "wiki_page_$key",
                        title = title,
                        thumbnailUrl = thumbUrl,
                        fullUrl = fullUrl,
                        width = thumbnailObj?.optInt("width", 800) ?: 800,
                        height = thumbnailObj?.optInt("height", 600) ?: 600,
                        source = "Wikipedia",
                        description = extract
                    )
                )
            }
        }
        return list
    }

    private fun fetchWikipediaSummaryImage(term: String): SearchImageResult? {
        val cleanTerm = term.replace(" ", "_").trim()
        val url = "https://en.wikipedia.org/api/rest_v1/page/summary/${URLEncoder.encode(cleanTerm, "UTF-8")}"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", BROWSER_USER_AGENT)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return null
        val bodyStr = response.body?.string() ?: return null
        val json = JSONObject(bodyStr)

        val thumbnailObj = json.optJSONObject("thumbnail")
        val originalObj = json.optJSONObject("originalimage")
        val thumbUrl = thumbnailObj?.optString("source") ?: originalObj?.optString("source") ?: return null
        val fullUrl = originalObj?.optString("source") ?: thumbUrl
        val title = json.optString("title", term)
        val extract = json.optString("extract", "")

        return SearchImageResult(
            id = "wiki_summary_${title.hashCode()}",
            title = title,
            thumbnailUrl = thumbUrl,
            fullUrl = fullUrl,
            width = thumbnailObj.optInt("width", 800),
            height = thumbnailObj.optInt("height", 600),
            source = "Wikipedia",
            description = extract.take(160)
        )
    }

    private fun fetchWikimediaCommonsImages(term: String, limit: Int): List<SearchImageResult> {
        val list = mutableListOf<SearchImageResult>()
        val encoded = URLEncoder.encode(term, "UTF-8")
        val wikiUrl = "https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch=$encoded&gsrlimit=$limit&gsrnamespace=6&prop=imageinfo&iiprop=url|size|mime&iiurlwidth=800&format=json&origin=*"

        val request = Request.Builder()
            .url(wikiUrl)
            .header("User-Agent", BROWSER_USER_AGENT)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()
        val jsonStr = response.body?.string() ?: return emptyList()
        val root = JSONObject(jsonStr)
        val queryObj = root.optJSONObject("query")
        val pagesObj = queryObj?.optJSONObject("pages") ?: return emptyList()

        val keys = pagesObj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val page = pagesObj.optJSONObject(key) ?: continue
            val imageInfoArray = page.optJSONArray("imageinfo") ?: continue
            if (imageInfoArray.length() == 0) continue

            val info = imageInfoArray.getJSONObject(0)
            val thumbUrl = info.optString("thumburl").ifEmpty { info.optString("url") }
            val fullUrl = info.optString("url")
            val width = info.optInt("width", 800)
            val height = info.optInt("height", 600)

            if (thumbUrl.isNotBlank()) {
                val rawTitle = page.optString("title", "").replace("File:", "").replace(Regex("\\.(jpg|jpeg|png|webp|svg)$", RegexOption.IGNORE_CASE), "").replace("_", " ").trim()
                list.add(
                    SearchImageResult(
                        id = "commons_$key",
                        title = rawTitle.ifEmpty { term },
                        thumbnailUrl = thumbUrl,
                        fullUrl = if (fullUrl.endsWith(".svg", ignoreCase = true)) thumbUrl else fullUrl,
                        width = width,
                        height = height,
                        source = "Wikimedia Commons",
                        description = rawTitle
                    )
                )
            }
        }
        return list
    }
}


