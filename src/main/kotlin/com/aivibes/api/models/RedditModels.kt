package com.aivibes.api.models

import kotlinx.serialization.Serializable

@Serializable
data class RedditResponse(
    val data: RedditData
)

@Serializable
data class RedditData(
    val children: List<RedditPost>
)

@Serializable
data class RedditPost(
    val data: RedditPostData
)

@Serializable
data class RedditPostData(
    val id: String,
    val title: String,
    val selftext: String,
    val author: String,
    val created_utc: Double,
    val permalink: String,
    val thumbnail: String,
    val url: String? = null
)

@Serializable
data class RedditTokenResponse(val access_token: String, val token_type: String, val expires_in: Int, val scope: String)