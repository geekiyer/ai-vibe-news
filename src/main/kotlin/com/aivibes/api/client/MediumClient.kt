package com.aivibes.api.client

import com.aivibes.api.models.MediumItem
import com.aivibes.models.Article
import io.ktor.client.call.*
import io.ktor.client.request.*

class MediumClient {
    private val client = HttpClientFactory.client

    suspend fun fetchArticles(startId: Int): List<Article> {
        return try {
            println("Starting Medium fetch...")
            val rssContent = client.get("https://medium.com/feed/tag/artificial-intelligence") {
                headers {
                    append("User-Agent", "AI-Vibe-News/1.0")
                }
            }.body<String>()

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
            
            var currentId = startId
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
                        id = currentId++,
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