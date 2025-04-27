package com.aivibes.templates

import com.aivibes.models.Article
import io.ktor.server.html.*
import kotlinx.html.*

class HomePage(val articles: List<Article>) : Template<HTML> {
    override fun HTML.apply() {
        head {
            title { +"AI Vibe News" }
            meta { name = "viewport"; content = "width=device-width, initial-scale=1.0" }
            style {
                unsafe {
                    +"""
                        :root {
                            --primary-color: #2563eb;
                            --text-color: #1a1a1a;
                            --light-text: #666;
                            --card-bg: #ffffff;
                        }
                        
                        body {
                            font-family: Arial, sans-serif;
                            margin: 0;
                            padding: 0;
                            background-color: #f0f0f0;
                            color: var(--text-color);
                        }
                        
                        .header {
                            background-color: var(--primary-color);
                            color: white;
                            padding: 2rem;
                            text-align: center;
                            margin-bottom: 2rem;
                        }
                        
                        .header h1 {
                            margin: 0;
                            font-size: 2.5rem;
                        }
                        
                        .header p {
                            margin: 0.5rem 0 0;
                            font-size: 1.2rem;
                            opacity: 0.9;
                        }
                        
                        .container {
                            max-width: 1200px;
                            margin: 0 auto;
                            padding: 0 1rem;
                        }
                        
                        .article-grid {
                            display: grid;
                            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
                            gap: 2rem;
                            margin-top: 2rem;
                        }
                        
                        .article-card {
                            background: var(--card-bg);
                            border-radius: 1rem;
                            overflow: hidden;
                            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
                            transition: transform 0.3s ease;
                            cursor: pointer;
                            text-decoration: none;
                            color: inherit;
                        }
                        
                        .article-card:hover {
                            transform: translateY(-5px);
                        }
                        
                        .article-image {
                            width: 100%;
                            height: 200px;
                            object-fit: cover;
                        }
                        
                        .article-content {
                            padding: 1.5rem;
                        }
                        
                        .article-title {
                            font-size: 1.5rem;
                            font-weight: 600;
                            margin-bottom: 0.5rem;
                            color: var(--text-color);
                        }
                        
                        .article-meta {
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                            margin-bottom: 1rem;
                            color: var(--light-text);
                            font-size: 0.875rem;
                        }
                        
                        .article-tags {
                            display: flex;
                            flex-wrap: wrap;
                            gap: 0.5rem;
                            margin-top: 1rem;
                        }
                        
                        .tag {
                            background: #e0e7ff;
                            color: var(--primary-color);
                            padding: 0.25rem 0.75rem;
                            border-radius: 9999px;
                            font-size: 0.75rem;
                            font-weight: 500;
                        }
                        
                        .no-articles {
                            text-align: center;
                            padding: 2rem;
                            background: var(--card-bg);
                            border-radius: 1rem;
                            margin: 2rem auto;
                            max-width: 600px;
                        }
                        
                        @media (max-width: 768px) {
                            .container {
                                padding: 1rem;
                            }
                            
                            .header {
                                padding: 1.5rem;
                            }
                            
                            .header h1 {
                                font-size: 2rem;
                            }
                            
                            .article-grid {
                                grid-template-columns: 1fr;
                            }
                        }
                    """
                }
            }
        }
        body {
            div(classes = "header") {
                h1 { +"AI Vibe News" }
                p { +"Daily Insights into AI and Vibe Coding" }
            }
            
            div(classes = "container") {
                div(classes = "article-grid") {
                    if (articles.isEmpty()) {
                        div(classes = "no-articles") {
                            h2 { +"No articles found" }
                            p { +"Please check back later for new articles." }
                        }
                    } else {
                        articles.forEach { article ->
                            a(classes = "article-card", href = "/article/${article.id}") {
                                img(classes = "article-image") {
                                    src = article.imageUrl ?: "https://picsum.photos/800/400?random=${article.title.hashCode()}"
                                    alt = article.title
                                }
                                div(classes = "article-content") {
                                    h3(classes = "article-title") { +article.title }
                                    div(classes = "article-meta") {
                                        span { +"By ${article.author}" }
                                        span { +" | " }
                                        span { +article.source }
                                    }
                                    p { +article.content.take(200).plus("...") }
                                    div(classes = "article-tags") {
                                        article.tags.take(3).forEach { tag ->
                                            span(classes = "tag") { +tag }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 