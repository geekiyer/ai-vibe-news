package com.aivibes.services

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
                    val hackernewsArticles = fetchHackerNewsArticles()
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
            
            // Wait for all fetches to complete and combine results
            val results = awaitAll(hackerNewsDeferred, devToDeferred, redditDeferred, mediumDeferred)
            articles.addAll(results.flatten())
            
            println("=== Total Articles: ${articles.size} ===\n")
            articles.sortedByDescending { it.publishedAt }
        }
    }

    private suspend fun fetchHackerNewsArticles(): List<Article> {
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
            println("Starting Reddit fetch...")
            checkRedditRateLimit() // Check rate limit before making request
            
            val response = client.get("https://www.reddit.com/r/artificialinteligence/top.json") {
                url {
                    parameters.append("limit", "15")
                    parameters.append("t", "day") // Get top posts from today
                }
                headers {
                    append("User-Agent", "AI-Vibe-News/1.0 (by /u/giyer7)")
                    append("Accept", "application/json")
                    append("Accept-Language", "en-US,en;q=0.9")
                }
            }
            
            val redditResponse = response.body<RedditResponse>()
            println("Raw Reddit response: ${redditResponse.data.children.size} posts found")
            
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
    val cover_image: String? = null,
    val path: String
)

@kotlinx.serialization.Serializable
private data class DevToUser(
    val name: String
)

@kotlinx.serialization.Serializable
private data class RedditResponse(
    val data: RedditData
)

@kotlinx.serialization.Serializable
private data class RedditData(
    val children: List<RedditPost>
)

@kotlinx.serialization.Serializable
private data class RedditPost(
    val data: RedditPostData
)

@kotlinx.serialization.Serializable
private data class RedditPostData(
    val id: String,
    val title: String,
    val selftext: String,
    val author: String,
    val created_utc: Double,
    val permalink: String,
    val thumbnail: String,
    val url: String? = null
)

@kotlinx.serialization.Serializable
private data class MediumItem(
    val title: String,
    val description: String,
    val author: String,
    val pubDate: String,
    val link: String,
    val thumbnail: String?,
    val guid: String
) 