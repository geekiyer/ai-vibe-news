package com.aivibes.models

import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class Article(
    val id: Int = 0,
    val title: String,
    val content: String,
    val author: String,
    val publishedAt: String,
    val source: String,
    val tags: List<String>,
    val url: String? = null,
    val imageUrl: String? = null
) {
    fun getFormattedPublishedTime(): String {
        return try {
            // Try different date formats
            val dateTime = when {
                // Handle "Sun, 27 Apr 2025 05:26:46 GMT" format
                publishedAt.contains("GMT") -> {
                    val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z")
                    ZonedDateTime.parse(publishedAt, formatter)
                        .withZoneSameInstant(ZoneId.systemDefault())
                }
                // ISO format with timezone (including Z for UTC)
                publishedAt.contains("T") && publishedAt.contains("Z") -> {
                    ZonedDateTime.parse(publishedAt)
                        .withZoneSameInstant(ZoneId.systemDefault())
                }
                // ISO format with explicit timezone
                publishedAt.contains("T") && publishedAt.contains("+") -> {
                    ZonedDateTime.parse(publishedAt)
                        .withZoneSameInstant(ZoneId.systemDefault())
                }
                // ISO format without timezone (assume UTC)
                publishedAt.contains("T") -> {
                    LocalDateTime.parse(publishedAt, DateTimeFormatter.ISO_DATE_TIME)
                        .atZone(ZoneId.of("UTC"))
                        .withZoneSameInstant(ZoneId.systemDefault())
                }
                // Unix timestamp (in seconds)
                publishedAt.matches(Regex("\\d+")) -> {
                    ZonedDateTime.ofInstant(
                        java.time.Instant.ofEpochSecond(publishedAt.toLong()),
                        ZoneId.systemDefault()
                    )
                }
                // Fallback to simple date format
                else -> {
                    LocalDateTime.parse(publishedAt, DateTimeFormatter.ISO_DATE_TIME)
                        .atZone(ZoneId.systemDefault())
                }
            }

            val now = ZonedDateTime.now(ZoneId.systemDefault())
            val duration = Duration.between(dateTime, now)
            
            when {
                duration.toMinutes() < 60 -> "Published ${duration.toMinutes()} minutes ago"
                duration.toHours() < 24 -> "Published ${duration.toHours()} hours ago"
                duration.toDays() < 7 -> "Published ${duration.toDays()} days ago"
                else -> "Published on ${dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}"
            }
        } catch (e: Exception) {
            // If all parsing fails, try to extract just the date part
            try {
                val datePart = publishedAt.split("T")[0]
                val date = java.time.LocalDate.parse(datePart)
                "Published on ${date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}"
            } catch (e: Exception) {
                "Published on $publishedAt"
            }
        }
    }
}