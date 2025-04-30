package com.aivibes.api.models

import kotlinx.serialization.Serializable

@Serializable
data class DevToArticle(
    val title: String,
    val body_markdown: String? = null,
    val description: String? = null,
    val published_at: String,
    val user: DevToUser,
    val tag_list: List<String>,
    val cover_image: String? = null,
    val path: String
)

@Serializable
data class DevToUser(
    val name: String
) 