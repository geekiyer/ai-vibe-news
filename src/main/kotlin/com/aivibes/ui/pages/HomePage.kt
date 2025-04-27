package com.aivibes.ui.pages

import com.aivibes.services.ArticleService
import kotlinx.coroutines.runBlocking
import kotlinx.html.*

class HomePage(private val articleService: ArticleService) {
    fun render(): HTML.() -> Unit = {
        println("Starting render function...")
        head {
            title { +"AI Vibe News" }
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
            println("Rendering body...")
            h1 { +"AI Vibe News" }
            p { +"Daily Insights into AI and Vibe Coding" }
            
            println("Fetching articles...")
            val articles = runBlocking { articleService.fetchLatestArticles() }
            println("Found ${articles.size} articles")
            
            if (articles.isEmpty()) {
                println("No articles found, rendering empty state")
                p { +"No articles found" }
            } else {
                println("Rendering ${articles.size} articles")
                articles.forEach { article ->
                    println("Rendering article: ${article.title}")
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