package com.aivibes.api.client

import com.aivibes.api.models.HackerNewsStory
import com.aivibes.models.Article
import io.ktor.client.call.*
import io.ktor.client.request.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HackerNewsClient {
    private val client = HttpClientFactory.client

    suspend fun fetchArticles(startId: Int): List<Article> {
        return try {
            val storyIds = client.get("https://hacker-news.firebaseio.com/v0/topstories.json").body<List<Int>>()
            
            var currentId = startId
            storyIds.take(15).mapNotNull { storyId ->
                val story = client.get("https://hacker-news.firebaseio.com/v0/item/$storyId.json").body<HackerNewsStory>()
                
                if (story.title.contains("AI", ignoreCase = true) || 
                    story.title.contains("vibe", ignoreCase = true) ||
                    story.title.contains("coding", ignoreCase = true)) {
                    Article(
                        id = currentId++,
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