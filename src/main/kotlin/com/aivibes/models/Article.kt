package com.aivibes.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Article(
    val id: Int,
    val title: String,
    val content: String,
    val author: String,
    val publishDate: String,
    val tags: List<String>,
    val imageUrl: String? = null
) 