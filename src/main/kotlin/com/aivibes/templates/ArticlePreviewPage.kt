package com.aivibes.templates

import com.aivibes.models.Article
import io.ktor.http.*
import io.ktor.server.html.*
import kotlinx.html.*

class ArticlePreviewPage(val article: Article) : Template<HTML> {
    override fun HTML.apply() {
        head {
            title { +"${article.title} - vibeai.news" }
            
            // Standard meta tags
            meta { name = "viewport"; content = "width=device-width, initial-scale=1.0" }
            meta { name = "description"; content = article.content.take(160) }
            meta { charset = "UTF-8" }
            
            // Open Graph meta tags
            meta { attributes["property"] = "og:title"; content = article.title }
            meta { attributes["property"] = "og:description"; content = article.content.take(200) }
            meta { attributes["property"] = "og:type"; content = "article" }
            meta { attributes["property"] = "og:url"; content = "https://vibeai.news/a/${article.id}" }
            meta { attributes["property"] = "og:image"; content = article.imageUrl ?: "https://vibeai.news/logo.jpg" }
            meta { attributes["property"] = "og:site_name"; content = "vibeai.news" }
            
            // Twitter Card meta tags
            meta { name = "twitter:card"; content = "summary_large_image" }
            meta { name = "twitter:title"; content = article.title }
            meta { name = "twitter:description"; content = article.content.take(200) }
            meta { name = "twitter:image"; content = article.imageUrl ?: "https://vibeai.news/logo.jpg" }
            meta { name = "twitter:site"; content = "@vibeainews" }
            
            // Stylesheets
            link {
                rel = "stylesheet"
                href = "https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css"
            }
            
            // Auto-redirect after 3 seconds
            script {
                unsafe {
                    raw("""
                        setTimeout(() => {
                            window.location.href = '${article.url}';
                        }, 3000);
                    """)
                }
            }
            
            // Analytics script (optional)
            script(type = "text/javascript") {
                unsafe {
                    raw("""
                        function recordAnalytics() {
                            fetch('/api/record-click', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({
                                    articleId: '${article.id}',
                                    referrer: document.referrer,
                                    platform: new URLSearchParams(window.location.search).get('utm_source')
                                })
                            });
                        }
                        window.onload = recordAnalytics;
                    """)
                }
            }
        }
        body(classes = "bg-gray-50") {
            div(classes = "min-h-screen flex items-center justify-center p-4") {
                div(classes = "bg-white rounded-lg shadow-xl max-w-2xl w-full p-6") {
                    // Progress bar
                    div(classes = "w-full bg-gray-200 rounded-full h-1.5 mb-6") {
                        div(classes = "bg-blue-600 h-1.5 rounded-full w-0") {
                            attributes["style"] = "animation: progress 3s linear forwards;"
                        }
                    }
                    
                    img(
                        classes = "w-full h-64 object-cover rounded-lg mb-6",
                        src = article.imageUrl ?: "https://vibeai.news/logo.jpg",
                        alt = article.title
                    )
                    
                    h1(classes = "text-2xl font-bold mb-4") { +article.title }
                    
                    div(classes = "flex items-center gap-2 text-sm text-gray-600 mb-4") {
                        span { +"By ${article.author}" }
                        span { +"•" }
                        span { +article.source }
                        span { +"•" }
                        span { +article.getFormattedPublishedTime() }
                    }
                    
                    p(classes = "text-gray-700 mb-6") { +article.content.take(200).plus("...") }
                    
                    a(href = article.url ?: "#", classes = "block w-full") {
                        button(classes = "w-full bg-blue-600 text-white rounded-lg px-4 py-2 hover:bg-blue-700 transition-colors") {
                            +"Continue to Article"
                        }
                    }
                    
                    p(classes = "text-center text-sm text-gray-500 mt-4") {
                        +"Redirecting you in 3 seconds..."
                    }
                }
            }
            
            style {
                +"""
                    @keyframes progress {
                        from { width: 0; }
                        to { width: 100%; }
                    }
                """
            }
        }
    }
} 