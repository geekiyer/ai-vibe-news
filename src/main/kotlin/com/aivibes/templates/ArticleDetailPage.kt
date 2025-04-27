package com.aivibes.templates

import com.aivibes.models.Article
import io.ktor.server.html.*
import kotlinx.html.*

class ArticleDetailPage(val article: Article) : Template<HTML> {
    override fun HTML.apply() {
        head {
            title { +"${article.title} - AI Vibe News" }
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
                            padding: 1rem 2rem;
                            display: flex;
                            align-items: center;
                            gap: 1rem;
                        }
                        
                        .back-button {
                            color: white;
                            text-decoration: none;
                            font-size: 1.2rem;
                            padding: 0.5rem;
                            border-radius: 0.5rem;
                            transition: background-color 0.3s ease;
                        }
                        
                        .back-button:hover {
                            background-color: rgba(255, 255, 255, 0.1);
                        }
                        
                        .container {
                            max-width: 800px;
                            margin: 0 auto;
                            padding: 2rem 1rem;
                        }
                        
                        .article-header {
                            background: var(--card-bg);
                            border-radius: 1rem;
                            padding: 2rem;
                            margin-bottom: 2rem;
                            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
                        }
                        
                        .article-image {
                            width: 100%;
                            height: 400px;
                            object-fit: cover;
                            border-radius: 0.5rem;
                            margin-bottom: 1.5rem;
                        }
                        
                        .article-title {
                            font-size: 2.5rem;
                            font-weight: 700;
                            margin-bottom: 1rem;
                            color: var(--text-color);
                        }
                        
                        .article-meta {
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                            margin-bottom: 1.5rem;
                            color: var(--light-text);
                            font-size: 1rem;
                        }
                        
                        .article-content {
                            background: var(--card-bg);
                            border-radius: 1rem;
                            padding: 2rem;
                            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
                            line-height: 1.8;
                            font-size: 1.1rem;
                        }
                        
                        .article-tags {
                            display: flex;
                            flex-wrap: wrap;
                            gap: 0.5rem;
                            margin-top: 1.5rem;
                        }
                        
                        .tag {
                            background: #e0e7ff;
                            color: var(--primary-color);
                            padding: 0.5rem 1rem;
                            border-radius: 9999px;
                            font-size: 0.875rem;
                            font-weight: 500;
                        }
                        
                        @media (max-width: 768px) {
                            .container {
                                padding: 1rem;
                            }
                            
                            .article-header {
                                padding: 1.5rem;
                            }
                            
                            .article-title {
                                font-size: 2rem;
                            }
                            
                            .article-image {
                                height: 300px;
                            }
                        }
                    """
                }
            }
        }
        body {
            div(classes = "header") {
                a(classes = "back-button", href = "/") { +"← Back to Home" }
                h1 { +"AI Vibe News" }
            }
            
            div(classes = "container") {
                div(classes = "article-header") {
                    img(classes = "article-image") {
                        src = article.imageUrl ?: "https://picsum.photos/800/400?random=${article.title.hashCode()}"
                        alt = article.title
                    }
                    h1(classes = "article-title") { +article.title }
                    div(classes = "article-meta") {
                        span { +"By ${article.author}" }
                        span { +" | " }
                        span { +article.source }
                        span { +" | " }
                        span { +article.publishedAt }
                    }
                    div(classes = "article-tags") {
                        article.tags.forEach { tag ->
                            span(classes = "tag") { +tag }
                        }
                    }
                }
                
                div(classes = "article-content") {
                    unsafe { +article.content }
                }
            }
        }
    }
} 