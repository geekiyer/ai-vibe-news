package com.aivibes.services

import com.aivibes.api.HackerNewsClient
import com.aivibes.api.models.DevToArticle
import com.aivibes.api.models.MediumItem
import com.aivibes.api.models.RedditResponse
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
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

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

    // Rate limiting for Reddit API
    private val redditRequestCount = AtomicInteger(0)
    private val lastRedditRequestTime = ConcurrentHashMap<String, Long>()
    private val REDDIT_RATE_LIMIT = 10 // requests per minute
    private val REDDIT_WINDOW_MS = 60_000L // 1 minute in milliseconds

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
                    val redditArticles = fetchRedditArticles()
                    println("✓ Reddit: ${redditArticles.size} articles")
                    redditArticles
                } catch (e: Exception) {
                    println("✗ Error fetching Reddit articles: ${e.message}")
                    emptyList()
                }
            }

            val mediumDeferred = async {
                try {
                    val mediumArticles = fetchMediumArticles()
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

    private suspend fun fetchRedditArticles(): List<Article> {
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

    private suspend fun fetchMediumArticles(): List<Article> {
        return try {
            println("Starting Medium fetch...")
            val response = client.get("https://medium.com/feed/tag/artificial-intelligence") {
                headers {
                    append("User-Agent", "AI-Vibe-News/1.0")
                }
            }
            
            val rssContent = response.body<String>()
            println("Medium RSS content received: ${rssContent.length} characters")
            
            // Parse the RSS content manually
            val items = mutableListOf<MediumItem>()
            val itemRegex = Regex("<item>(.*?)</item>", RegexOption.DOT_MATCHES_ALL)
            val titleRegex = Regex("<title>(?:<!\\[CDATA\\[)?(.*?)(?:]]>)?</title>")
            val descriptionRegex = Regex("<description>(?:<!\\[CDATA\\[)?(.*?)(?:]]>)?</description>")
            val authorRegex = Regex("<dc:creator>(?:<!\\[CDATA\\[)?(.*?)(?:]]>)?</dc:creator>")
            val pubDateRegex = Regex("<pubDate>(.*?)</pubDate>")
            val linkRegex = Regex("<link>(.*?)</link>")
            val guidRegex = Regex("<guid>(.*?)</guid>")
            val imageRegex = Regex("<img[^>]+src=\"([^\">]+)\"")
            
            itemRegex.findAll(rssContent).forEach { match ->
                val itemContent = match.groupValues[1]
                val title = titleRegex.find(itemContent)?.groupValues?.get(1)?.trim() ?: "Untitled"
                val description = descriptionRegex.find(itemContent)?.groupValues?.get(1)?.trim() ?: ""
                val author = authorRegex.find(itemContent)?.groupValues?.get(1)?.trim() ?: "Unknown Author"
                val pubDate = pubDateRegex.find(itemContent)?.groupValues?.get(1)?.trim() ?: ""
                val link = linkRegex.find(itemContent)?.groupValues?.get(1)?.trim() ?: ""
                val guid = guidRegex.find(itemContent)?.groupValues?.get(1)?.trim() ?: ""
                
                // Extract image from description
                val imageUrl = imageRegex.find(description)?.groupValues?.get(1)
                
                // Clean up HTML tags from description
                val cleanDescription = description
                    .replace(Regex("<[^>]+>"), "") // Remove HTML tags
                    .replace(Regex("&[^;]+;"), "") // Remove HTML entities
                    .trim()
                
                items.add(MediumItem(
                    title = title,
                    description = cleanDescription,
                    author = author,
                    pubDate = pubDate,
                    link = link,
                    thumbnail = imageUrl,
                    guid = guid
                ))
            }
            
            println("Found ${items.size} Medium articles in RSS feed")
            
            val articles = items.mapNotNull { item ->
                try {
                    println("Processing Medium article: ${item.title}")
                    val content = item.description.takeIf { it.isNotBlank() } ?: item.title
                    
                    // Generate a more varied fallback image based on the article's title
                    val fallbackImage = when {
                        item.title.contains("AI", ignoreCase = true) -> "https://picsum.photos/800/400?random=ai${item.guid}"
                        item.title.contains("Machine Learning", ignoreCase = true) -> "https://picsum.photos/800/400?random=ml${item.guid}"
                        item.title.contains("Deep Learning", ignoreCase = true) -> "https://picsum.photos/800/400?random=dl${item.guid}"
                        else -> "https://picsum.photos/800/400?random=${item.guid}"
                    }
                    
                    Article(
                        title = item.title,
                        content = content,
                        author = item.author,
                        publishedAt = item.pubDate,
                        source = "Medium",
                        tags = listOf("AI", "Medium"),
                        imageUrl = item.thumbnail ?: fallbackImage,
                        url = item.link
                    )
                } catch (e: Exception) {
                    println("Error processing Medium article: ${e.message}")
                    null
                }
            }
            println("Successfully processed ${articles.size} Medium articles")
            articles
        } catch (e: Exception) {
            println("Error in fetchMediumArticles: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
} 