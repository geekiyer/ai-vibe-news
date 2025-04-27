package com.aivibes.services

import com.aivibes.models.Article
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ArticleService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun fetchLatestArticles(): List<Article> {
        println("Starting to fetch latest articles...")
        val articles = mutableListOf<Article>()
        
        // Fetch from Hacker News API
        println("Fetching Hacker News articles...")
        val hackerNewsArticles = fetchHackerNewsArticles()
        println("Found ${hackerNewsArticles.size} Hacker News articles")
        articles.addAll(hackerNewsArticles)

        // Fetch from Dev.to API
        println("Fetching Dev.to articles...")
        val devToArticles = fetchDevToArticles()
        println("Found ${devToArticles.size} Dev.to articles")
        articles.addAll(devToArticles)

        println("Total articles fetched: ${articles.size}")
        println("Article titles: ${articles.map { it.title }}")
        // Normalize and sort articles by date
        return articles.sortedByDescending { it.publishedAt }
    }

    private suspend fun fetchHackerNewsArticles(): List<Article> {
        return try {
            println("Fetching Hacker News top stories...")
            val storyIds = client.get("https://hacker-news.firebaseio.com/v0/topstories.json")
            println("Hacker News response status: ${storyIds.status}")
            println("Hacker News response: ${storyIds.bodyAsText()}")
            
            val ids = storyIds.body<List<Int>>()
            println("Found ${ids.size} story IDs")
            
            ids.take(10).mapNotNull { storyId ->
                println("Fetching Hacker News story $storyId...")
                val story = client.get("https://hacker-news.firebaseio.com/v0/item/$storyId.json").body<HackerNewsStory>()
                println("Story $storyId response: $story")
                
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
                        imageUrl = "https://picsum.photos/800/400?random=$storyId"
                    )
                } else {
                    println("Story $storyId doesn't match keywords")
                    null
                }
            }
        } catch (e: Exception) {
            println("Error fetching Hacker News articles: ${e.message}")
            println("Stack trace:")
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun fetchDevToArticles(): List<Article> {
        return try {
            println("Fetching Dev.to articles...")
            val response = client.get("https://dev.to/api/articles") {
                url {
                    parameters.append("tag", "ai")
                    parameters.append("per_page", "10")
                }
                headers {
                    append("Accept", "application/json")
                    append("User-Agent", "AI-Vibe-News/1.0")
                }
            }
            
            println("Dev.to request URL: ${response.request.url}")
            println("Dev.to request headers: ${response.request.headers}")
            println("Dev.to response status: ${response.status}")
            val responseBody = response.bodyAsText()
            println("Dev.to response body: $responseBody")
            
            val articles = response.body<List<DevToArticle>>()
            println("Parsed Dev.to articles: $articles")
            
            articles.map { devToArticle ->
                val content = devToArticle.body_markdown ?: devToArticle.description ?: "No content available"
                Article(
                    title = devToArticle.title,
                    content = content,
                    author = devToArticle.user.name,
                    publishedAt = devToArticle.published_at,
                    source = "Dev.to",
                    tags = devToArticle.tag_list,
                    imageUrl = devToArticle.cover_image ?: "https://picsum.photos/800/400?random=${devToArticle.title.hashCode()}"
                )
            }
        } catch (e: Exception) {
            println("Error fetching Dev.to articles: ${e.message}")
            println("Stack trace:")
            e.printStackTrace()
            emptyList()
        }
    }
}

// Data classes for API responses
@kotlinx.serialization.Serializable
private data class HackerNewsStory(
    val id: Int,
    val title: String,
    val url: String?,
    val by: String,
    val time: Long
)

@kotlinx.serialization.Serializable
private data class DevToArticle(
    val title: String,
    val body_markdown: String? = null,
    val description: String? = null,
    val published_at: String,
    val user: DevToUser,
    val tag_list: List<String>,
    val cover_image: String? = null
)

@kotlinx.serialization.Serializable
private data class DevToUser(
    val name: String
) 