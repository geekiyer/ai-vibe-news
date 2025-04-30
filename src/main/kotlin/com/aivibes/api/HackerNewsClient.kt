package com.aivibes.api

import com.aivibes.api.models.HackerNewsStory
import com.aivibes.models.Article
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HackerNewsClient {
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
    }

    suspend fun fetchArticles(): List<Article> {
        return try {
            val storyIds = client.get("https://hacker-news.firebaseio.com/v0/topstories.json").body<List<Int>>()
            
            storyIds.take(15).mapNotNull { storyId ->
                val story = client.get("https://hacker-news.firebaseio.com/v0/item/$storyId.json").body<HackerNewsStory>()
                
                if (story.title.contains("AI", ignoreCase = true) || 
                    story.title.contains("vibe", ignoreCase = true) ||
                    story.title.contains("coding", ignoreCase = true)) {
                    Article(
                        title = story.title,
                        content = story.url ?: "",
                        author = "Hacker News User",
                        publishedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                        source = "Hacker News",
                        tags = listOf("AI", "Technology"),
                        imageUrl = "https://picsum.photos/800/400?random=$storyId",
                        url = story.url
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            println("Error in fetchHackerNewsArticles: ${e.message}")
            emptyList()
        }
    }
} 