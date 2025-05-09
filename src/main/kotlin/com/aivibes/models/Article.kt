package com.aivibes.models

import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Serializable
data class Article(
    val id: Int,
    val title: String,
    val content: String,
    val author: String,
    val publishedAt: String,
    val tags: List<String>,
    val imageUrl: String? = null,
    val source: String,
    val url: String? = null
) {
    fun getFormattedPublishedTime(): String {
        return try {
            val publishedInstant = Instant.parse(publishedAt)
            val now = Instant.now()
            val minutes = ChronoUnit.MINUTES.between(publishedInstant, now)
            val hours = ChronoUnit.HOURS.between(publishedInstant, now)
            val days = ChronoUnit.DAYS.between(publishedInstant, now)

            when {
                minutes < 60 -> "$minutes minutes ago"
                hours < 24 -> "$hours hours ago"
                days < 7 -> "$days days ago"
                else -> {
                    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
                    LocalDateTime.ofInstant(publishedInstant, ZoneId.systemDefault()).format(formatter)
                }
            }
        } catch (e: Exception) {
            publishedAt // Fallback to raw date if parsing fails
        }
    }
}