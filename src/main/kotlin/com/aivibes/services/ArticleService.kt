package com.aivibes.services

import com.aivibes.api.HackerNewsClient
import com.aivibes.api.MediumClient
import com.aivibes.api.RedditClient
import com.aivibes.api.models.DevToArticle
import com.aivibes.models.Article
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

class ArticleService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val hackerNewsClient = HackerNewsClient()
    private val mediumClient = MediumClient()
    private val redditClient = RedditClient()

    suspend fun fetchLatestArticles(): List<Article> {
        println("\n=== Starting Article Fetch ===")
        
        return coroutineScope {
            val articles = mutableListOf<Article>()
            
            // Launch all fetches in parallel
            val hackerNewsDeferred = async { 
                try {
                    val hackernewsArticles = hackerNewsClient.fetchArticles()
                    println("✓ Hacker News: ${hackernewsArticles.size} articles")
                    hackernewsArticles
                } catch (e: Exception) {
                    println("✗ Error fetching Hacker News articles: ${e.message}")
                    emptyList()
                }
            }
            
            val devToDeferred = async {
                try {
                    val devToArticles = fetchDevToArticles()
                    println("✓ Dev.to: ${devToArticles.size} articles")
                    devToArticles
                } catch (e: Exception) {
                    println("✗ Error fetching Dev.to articles: ${e.message}")
                    emptyList()
                }
            }
            
            val redditDeferred = async {
                try {
                    val redditArticles = redditClient.fetchArticles()
                    println("✓ Reddit: ${redditArticles.size} articles")
                    redditArticles
                } catch (e: Exception) {
                    println("✗ Error fetching Reddit articles: ${e.message}")
                    emptyList()
                }
            }

            val mediumDeferred = async {
                try {
                    val mediumArticles = mediumClient.fetchArticles()
                    println("✓ Medium: ${mediumArticles.size} articles")
                    mediumArticles
                } catch (e: Exception) {
                    println("✗ Error fetching Medium articles: ${e.message}")
                    emptyList()
                }
            }
            
            // Wait for all fetches to complete
            val results = awaitAll(hackerNewsDeferred, devToDeferred, redditDeferred, mediumDeferred)
            
            // Combine all articles
            results.forEach { articles.addAll(it) }
            
            println("=== Article Fetch Complete ===")
            println("Total articles fetched: ${articles.size}")
            
            articles
        }
    }

    private suspend fun fetchDevToArticles(): List<Article> {
        return try {
            val response = client.get("https://dev.to/api/articles") {
                url {
                    parameters.append("tag", "ai")
                    parameters.append("per_page", "15")
                }
                headers {
                    append("Accept", "application/json")
                    append("User-Agent", "AI-Vibe-News/1.0")
                }
            }
            
            val articles = response.body<List<DevToArticle>>()
            
            articles.map { devToArticle ->
                val content = devToArticle.body_markdown ?: devToArticle.description ?: "No content available"
                Article(
                    title = devToArticle.title,
                    content = content,
                    author = devToArticle.user.name,
                    publishedAt = devToArticle.published_at,
                    source = "Dev.to",
                    tags = devToArticle.tag_list,
                    imageUrl = devToArticle.cover_image ?: "https://picsum.photos/800/400?random=${devToArticle.title.hashCode()}",
                    url = "https://dev.to${devToArticle.path}"
                )
            }
        } catch (e: Exception) {
            println("Error in fetchDevToArticles: ${e.message}")
            emptyList()
        }
    }
} 