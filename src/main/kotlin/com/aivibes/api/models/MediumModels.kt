package com.aivibes.api.models

import kotlinx.serialization.Serializable

@Serializable
data class MediumItem(
    val title: String,
    val description: String,
    val author: String,
    val pubDate: String,
    val link: String,
    val thumbnail: String?,
    val guid: String
) 