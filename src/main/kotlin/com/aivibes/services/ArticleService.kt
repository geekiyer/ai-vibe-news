package com.aivibes.services

import com.aivibes.api.client.DevToClient
import com.aivibes.api.client.HackerNewsClient
import com.aivibes.api.client.MediumClient
import com.aivibes.api.client.RedditClient
import com.aivibes.models.Article
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class ArticleService {
    private val hackerNewsClient = HackerNewsClient()
    private val mediumClient = MediumClient()
    private val redditClient = RedditClient()
    private val devToClient = DevToClient()

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
                    val devToArticles = devToClient.fetchArticles()
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
} 