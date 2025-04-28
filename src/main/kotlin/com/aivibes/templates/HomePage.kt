package com.aivibes.templates

import com.aivibes.models.Article
import io.ktor.server.html.*
import kotlinx.html.*

class HomePage(val articles: List<Article>) : Template<HTML> {
    override fun HTML.apply() {
        head {
            title { +"vibeai.news - Your Daily AI News Hub" }
            meta { name = "viewport"; content = "width=device-width, initial-scale=1.0" }
            link {
                rel = "icon"
                type = "image/svg+xml"
                href = "/static/favicon.svg"
            }
            link {
                rel = "stylesheet"
                href = "https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css"
            }
            link {
                rel = "stylesheet"
                href = "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css"
            }
            style {
                +"""
                    .logo-text {
                        color: #1E40AF;
                        font-weight: 700;
                    }
                    .logo-icon {
                        background: linear-gradient(135deg, #1E40AF 0%, #EA580C 100%);
                        -webkit-mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M3 3h18v18H3V3zm16 16V5H5v14h14zM7 7h10v4H7V7zm0 6h10v4H7v-4z'/%3E%3C/svg%3E") no-repeat center;
                        mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M3 3h18v18H3V3zm16 16V5H5v14h14zM7 7h10v4H7V7zm0 6h10v4H7v-4z'/%3E%3C/svg%3E") no-repeat center;
                    }
                    .accent-text {
                        color: #EA580C;
                    }
                """
            }
        }
        body {
            div(classes = "min-h-screen bg-gray-50") {
                // Header
                header(classes = "bg-white text-white shadow-md") {
                    div(classes = "container mx-auto px-4 py-6") {
                        div(classes = "flex items-center justify-center space-x-4") {
                            div(classes = "logo-icon w-10 h-10") {}
                            h1(classes = "text-4xl font-bold") {
                                span(classes = "logo-text") { +"vibe" }
                                span(classes = "accent-text") { +"ai" }
                                span(classes = "logo-text") { +".news" }
                            }
                        }
                        p(classes = "text-center text-gray-600 mt-2 text-xl") { +"Your Daily AI News Hub" }
                    }
                }

                // Main Content
                main(classes = "container mx-auto px-4 py-8") {
                    if (articles.isEmpty()) {
                        div(classes = "bg-white rounded-lg shadow-md p-8 text-center max-w-2xl mx-auto") {
                            h2(classes = "text-2xl font-semibold mb-2") { +"No articles found" }
                            p(classes = "text-gray-600") { +"Please check back later for new articles." }
                        }
                    } else {
                        div(classes = "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6") {
                            articles.forEach { article ->
                                a(
                                    classes = "block bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300 transform hover:-translate-y-1",
                                    href = article.url ?: article.content,
                                    target = "_blank"
                                ) {
                                    img(
                                        classes = "w-full h-48 object-cover",
                                        src = article.imageUrl ?: "https://picsum.photos/800/400?random=${article.title.hashCode()}",
                                        alt = article.title
                                    )
                                    div(classes = "p-6") {
                                        h3(classes = "text-xl font-semibold mb-2 text-gray-900") { +article.title }
                                        div(classes = "flex flex-wrap gap-2 text-sm text-gray-600 mb-3") {
                                            span { +"By ${article.author}" }
                                            span { +"•" }
                                            span { +article.source }
                                            span { +"•" }
                                            span(classes = "text-blue-600") { +article.getFormattedPublishedTime() }
                                        }
                                        p(classes = "text-gray-700 mb-4 line-clamp-3") { +article.content.take(200).plus("...") }
                                        div(classes = "flex flex-wrap gap-2") {
                                            article.tags.take(3).forEach { tag ->
                                                span(classes = "px-2 py-1 bg-blue-100 text-blue-800 text-xs font-medium rounded-full") { +tag }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Footer
                footer(classes = "bg-gray-800 text-white py-8 mt-12") {
                    div(classes = "container mx-auto px-4 text-center") {
                        div(classes = "flex items-center justify-center space-x-2 mb-4") {
                            div(classes = "logo-icon w-6 h-6") {}
                            p(classes = "text-sm") { +"© 2024 vibeai.news. All rights reserved." }
                        }
                        div(classes = "flex justify-center gap-4 mt-4") {
                            a(classes = "text-gray-400 hover:text-white transition-colors", href = "#") {
                                i(classes = "fab fa-github text-xl")
                            }
                            a(classes = "text-gray-400 hover:text-white transition-colors", href = "#") {
                                i(classes = "fab fa-twitter text-xl")
                            }
                            a(classes = "text-gray-400 hover:text-white transition-colors", href = "#") {
                                i(classes = "fab fa-linkedin text-xl")
                            }
                        }
                    }
                }
            }
        }
    }
} 