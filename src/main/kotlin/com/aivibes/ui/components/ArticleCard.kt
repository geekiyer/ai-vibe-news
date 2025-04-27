package com.aivibes.ui.components

import kotlinx.html.*

fun FlowContent.articleCard(
    title: String,
    date: String,
    author: String,
    excerpt: String,
    tags: List<String>,
    isFeatured: Boolean = false
) {
    div(classes = "article-card ${if (isFeatured) "featured-article" else ""}") {
        div(classes = "article-image")
        div(classes = "article-content") {
            h2(classes = "article-title") { +title }
            div(classes = "article-meta") {
                span(classes = "article-date") { +date }
                span(classes = "article-author") { +"By $author" }
            }
            p(classes = "article-excerpt") { +excerpt }
            div(classes = "tag-list") {
                tags.forEach { tag ->
                    span(classes = "tag") { +tag }
                }
            }
        }
    }
} 