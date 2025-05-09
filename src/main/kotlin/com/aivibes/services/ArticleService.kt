package com.aivibes.services

import com.aivibes.api.client.DevToClient
import com.aivibes.api.client.HackerNewsClient
import com.aivibes.api.client.MediumClient
import com.aivibes.api.client.RedditClient
import com.aivibes.database.DatabaseFactory
import com.aivibes.models.Article
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

class ArticleService {
    private val hackerNewsClient = HackerNewsClient()
    private val mediumClient = MediumClient()
    private val redditClient = RedditClient()
    private val devToClient = DevToClient()
    private val database = DatabaseFactory()

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun fetchLatestArticles(): List<Article> {
        val articles = mutableListOf<Article>()
        var nextId = 1 // Start with ID 1

        // Fetch from Hacker News
        try {
            val hnArticles = hackerNewsClient.fetchArticles(nextId)
            articles.addAll(hnArticles)
            nextId += hnArticles.size
            // Save to database
            hnArticles.forEach { article ->
                database.createArticle(article)
            }
        } catch (e: Exception) {
            println("Error fetching from Hacker News: ${e.message}")
        }

        // Fetch from Dev.to
        try {
            val devToArticles = devToClient.fetchArticles(nextId)
            articles.addAll(devToArticles)
            nextId += devToArticles.size
            // Save to database
            devToArticles.forEach { article ->
                database.createArticle(article)
            }
        } catch (e: Exception) {
            println("Error fetching from Dev.to: ${e.message}")
        }

        // Fetch from Reddit
        try {
            val redditArticles = redditClient.fetchArticles(nextId)
            articles.addAll(redditArticles)
            nextId += redditArticles.size
            // Save to database
            redditArticles.forEach { article ->
                database.createArticle(article)
            }
        } catch (e: Exception) {
            println("Error fetching from Reddit: ${e.message}")
        }

        // Fetch from Medium
        try {
            val mediumArticles = mediumClient.fetchArticles(nextId)
            articles.addAll(mediumArticles)
            nextId += mediumArticles.size
            // Save to database
            mediumArticles.forEach { article ->
                database.createArticle(article)
            }
        } catch (e: Exception) {
            println("Error fetching from Medium: ${e.message}")
        }

        println("Total articles with IDs: ${articles.size}")
        articles.forEach { article ->
            println("Article ID: ${article.id}, Title: ${article.title}")
        }

        return articles.sortedByDescending { it.publishedAt }
    }
} 