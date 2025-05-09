package com.aivibes.api.client

import com.aivibes.api.models.DevToArticle
import com.aivibes.models.Article
import io.ktor.client.call.*
import io.ktor.client.request.*

class DevToClient {
    private val client = HttpClientFactory.client

    suspend fun fetchArticles(startId: Int): List<Article> {
        return try {
            println("Starting Dev.to fetch...")
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
            println("Found ${articles.size} Dev.to articles")
            
            var currentId = startId
            articles.map { devToArticle ->
                val content = devToArticle.body_markdown ?: devToArticle.description ?: "No content available"
                Article(
                    id = currentId++,
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
            e.printStackTrace()
            emptyList()
        }
    }
} 