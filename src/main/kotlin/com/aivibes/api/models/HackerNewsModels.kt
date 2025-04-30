package com.aivibes.api.models

@kotlinx.serialization.Serializable
data class HackerNewsStory(
    val id: Int,
    val title: String,
    val url: String?,
    val by: String,
    val time: Long
)