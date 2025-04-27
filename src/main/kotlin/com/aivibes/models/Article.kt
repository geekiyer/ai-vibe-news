package com.aivibes.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

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
)

object Articles : Table() {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val content = text("content")
    val author = varchar("author", 100)
    val publishedAt = varchar("published_at", 50)
    val articleSource = varchar("source", 50)
    val url = varchar("url", 255).nullable()
    val imageUrl = varchar("image_url", 255).nullable()

    override val primaryKey = PrimaryKey(id)
} 