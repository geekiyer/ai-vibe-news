package com.aivibes.api

import com.aivibes.api.models.DevToArticle
import com.aivibes.models.Article
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class DevToClient {
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
            e.printStackTrace()
            emptyList()
        }
    }
} 