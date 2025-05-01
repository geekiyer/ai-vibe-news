package com.aivibes.api

import com.aivibes.api.models.RedditResponse
import com.aivibes.models.Article
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class RedditClient {
    companion object {
        private val client: HttpClient by lazy {
            HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    })
                }
                install(Logging) {
                    level = LogLevel.INFO
                }
                engine {
                    maxConnectionsCount = 1000
                    endpoint {
                        maxConnectionsPerRoute = 100
                        pipelineMaxSize = 20
                        keepAliveTime = 5000
                        connectTimeout = 5000
                        requestTimeout = 15000
                    }
                }
            }
        }

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

    suspend fun fetchArticles(): List<Article> {
        return try {
            checkRedditRateLimit()
            
            val redditResponse = client.get("https://www.reddit.com/r/artificialinteligence/top.json") {
                url {
                    parameters.append("limit", "15")
                    parameters.append("t", "day")
                }
                headers {
                    append("User-Agent", "AI-Vibe-News/1.0")
                }
            }.body<RedditResponse>()
            
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
                        title = post.data.title,
                        content = content,
                        author = post.data.author,
                        publishedAt = LocalDateTime.ofEpochSecond(post.data.created_utc.toLong(), 0, java.time.ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_DATE_TIME),
                        source = "Reddit r/artificialinteligence",
                        tags = listOf("AI", "Reddit"),
                        imageUrl = imageUrl,
                        url = "https://reddit.com${post.data.permalink}"
                    )
                } catch (e: Exception) {
                    println("Error processing Reddit post '${post.data.title}': ${e.message}")
                    null
                }
            }
            println("Successfully processed ${articles.size} Reddit articles (${redditResponse.data.children.size - articles.size} filtered out)")
            articles
        } catch (e: Exception) {
            println("Error in fetchRedditArticles: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
} 