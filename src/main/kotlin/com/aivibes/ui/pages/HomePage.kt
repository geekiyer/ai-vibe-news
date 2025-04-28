package com.aivibes.ui.pages

import com.aivibes.services.ArticleService
import kotlinx.coroutines.runBlocking
import kotlinx.html.*

class HomePage(private val articleService: ArticleService) {
    fun render(): HTML.() -> Unit = {
        head {
            title { +"vibeai.news" }
            meta { name = "viewport"; content = "width=device-width, initial-scale=1.0" }
            style {
                unsafe {
                    +"""
                        body {
                            font-family: Arial, sans-serif;
                            margin: 0;
                            padding: 20px;
                        }
                        h1 {
                            color: blue;
                        }
                    """
                }
            }
        }
        body {
            h1 { +"vibeai.news" }
            p { +"Daily Insights into AI and Vibe Coding" }

            val articles = runBlocking { articleService.fetchLatestArticles() }
            
            if (articles.isEmpty()) {
                p { +"No articles found" }
            } else {
                articles.forEach { article ->
                    div {
                        h2 { +article.title }
                        p { +"By ${article.author}" }
                        p { +article.content.take(200).plus("...") }
                    }
                }
            }
        }
    }
} 