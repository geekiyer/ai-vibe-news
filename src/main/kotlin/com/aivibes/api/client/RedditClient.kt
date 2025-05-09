package com.aivibes.api.client

import com.aivibes.api.models.RedditResponse
import com.aivibes.api.models.RedditTokenResponse
import com.aivibes.models.Article
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class RedditClient {
    companion object {
        private val client = HttpClientFactory.client

        @Volatile private var cachedToken: String? = null
        private val tokenExpiry = AtomicLong(0L)

        // Rate limiting for Reddit API
        private val redditRequestCount = AtomicInteger(0)
        private val lastRedditRequestTime = ConcurrentHashMap<String, Long>()
        private const val REDDIT_RATE_LIMIT = 10 // requests per minute
        private const val REDDIT_WINDOW_MS = 60_000L // 1 minute in milliseconds

        private suspend fun checkRedditRateLimit() {
            val currentTime = System.currentTimeMillis()
            val lastRequestTime = lastRedditRequestTime.getOrDefault("reddit", 0L)
            
            // Reset counter if window has passed
            if (currentTime - lastRequestTime > REDDIT_WINDOW_MS) {
                redditRequestCount.set(0)
            }
            
            // Check if we've hit the rate limit
            if (redditRequestCount.get() >= REDDIT_RATE_LIMIT) {
                val timeToWait = REDDIT_WINDOW_MS - (currentTime - lastRequestTime)
                if (timeToWait > 0) {
                    println("Rate limit reached. Waiting ${timeToWait/1000} seconds...")
                    delay(timeToWait)
                    redditRequestCount.set(0)
                }
            }
            
            // Update request count and timestamp
            redditRequestCount.incrementAndGet()
            lastRedditRequestTime["reddit"] = currentTime
        }
    }

    private suspend fun getAccessToken(): String {
        val now = System.currentTimeMillis()
        // If token is valid for at least 1 more minute, reuse it
        if (cachedToken != null && now < tokenExpiry.get() - 60_000) {
            return cachedToken!!
        }
        // Otherwise, fetch a new token
        val clientId = System.getenv("REDDIT_CLIENT_ID") ?: "YOUR_CLIENT_ID"
        val clientSecret = System.getenv("REDDIT_CLIENT_SECRET") ?: "YOUR_CLIENT_SECRET"

        val response: RedditTokenResponse = client.submitForm(
            url = "https://www.reddit.com/api/v1/access_token",
            formParameters = Parameters.build {
                append("grant_type", "client_credentials")
            }
        ) {
            headers {
                append(HttpHeaders.Authorization, "Basic " + Base64.getEncoder()
                    .encodeToString("$clientId:$clientSecret".toByteArray()))
                append(HttpHeaders.UserAgent, "AI-Vibe-News/1.0")
            }
        }.body()

        cachedToken = response.access_token
        tokenExpiry.set(now + response.expires_in * 1000L)
        return cachedToken!!
    }

    suspend fun fetchArticles(startId: Int): List<Article> {
        return try {
            checkRedditRateLimit()
            val accessToken = getAccessToken()

            val redditResponse = client.get("https://oauth.reddit.com/r/artificialinteligence/top.json") {
                url {
                    parameters.append("limit", "15")
                    parameters.append("t", "day")
                }
                headers {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                    append("User-Agent", "AI-Vibe-News/1.0")
                }
            }.body<RedditResponse>()
            
            var currentId = startId
            val articles = redditResponse.data.children.mapNotNull { post ->
                try {
                    val content = post.data.selftext.takeIf { it.isNotBlank() } ?: post.data.title
                    if (content.isBlank()) {
                        println("Skipping post '${post.data.title}' - empty content")
                        return@mapNotNull null
                    }

                    val imageUrl = when {
                        post.data.thumbnail.startsWith("http") -> {
                            println("Using thumbnail URL for '${post.data.title}': ${post.data.thumbnail}")
                            post.data.thumbnail
                        }
                        post.data.url?.endsWith(".jpg") == true || post.data.url?.endsWith(".png") == true -> {
                            println("Using post URL as image for '${post.data.title}': ${post.data.url}")
                            post.data.url
                        }
                        else -> {
                            println("Using fallback image for '${post.data.title}'")
                            "https://picsum.photos/800/400?random=${post.data.id}"
                        }
                    }
                    
                    Article(
                        id = currentId++,
                        title = post.data.title,
                        content = content,
                        author = post.data.author,
                        publishedAt = LocalDateTime.ofEpochSecond(post.data.created_utc.toLong(), 0, ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_DATE_TIME),
                        source = "Reddit r/artificialinteligence",
                        tags = listOf("AI", "Reddit"),
                        imageUrl = imageUrl,
                        url = "https://reddit.com${post.data.permalink}"
                    )
                } catch (e: Exception) {
                    println("Error processing Reddit post: ${e.message}")
                    null
                }
            }
            println("Successfully processed ${articles.size} Reddit articles")
            articles
        } catch (e: Exception) {
            println("Error in fetchRedditArticles: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
} 