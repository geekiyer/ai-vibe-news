package com.aivibes.templates

import com.aivibes.models.Article
import io.ktor.http.*
import io.ktor.server.html.*
import kotlinx.html.*

class HomePage(val articles: List<Article>) : Template<HTML> {
    override fun HTML.apply() {
        head {
            title { +"vibeai.news - Your Daily AI News Hub" }
            meta { name = "viewport"; content = "width=device-width, initial-scale=1.0" }
            link(rel="apple-touch-icon", href="/apple-touch-icon.png", type=ContentType.Image.PNG.toString())
            link(rel="icon", href="/favicon-16x16.png", type=ContentType.Image.PNG.toString())
            link(rel="icon", href="/favicon-32x32.png", type=ContentType.Image.PNG.toString())
            link(rel="manifest", href="/site.webmanifest")
            link {
                rel = "stylesheet"
                href = "https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css"
            }
            link {
                rel = "stylesheet"
                href = "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css"
            }
            link {
                rel = "stylesheet"
                href = "/styles.css"
            }
            style {
                +"""
                    .logo-icon {
                        width: 250px;
                        height: 100px;
                        background-image: url('/logo.png');
                        background-size: contain;
                        background-repeat: no-repeat;
                        background-position: center;
                        border-radius: 1px;
                    }
                """
            }
        }
        body {
            div(classes = "min-h-screen bg-gray-50") {
                // Header
                header(classes = "bg-gray-800 text-white shadow-md") {
                    div(classes = "container mx-auto px-4 py-6") {
                        div(classes = "flex items-center justify-center space-x-4") {
                            div(classes = "logo-icon w-10 h-10") {}
                        }
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
                                div(classes = "bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300 transform hover:-translate-y-1") {
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
                                        div(classes = "flex flex-wrap gap-2 mb-4") {
                                            article.tags.take(3).forEach { tag ->
                                                span(classes = "px-2 py-1 bg-blue-100 text-blue-800 text-xs font-medium rounded-full") { +tag }
                                            }
                                        }
                                        div(classes = "flex justify-end") {
                                            button(classes = "share-button") {
                                                onClick = "openShareModal('${article.title.replace("'", "\\'")}', '${article.imageUrl ?: "https://picsum.photos/800/400?random=${article.title.hashCode()}"}')"
                                                i(classes = "fas fa-share-alt")
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
                        div(classes = "flex flex-col items-center space-y-2 mb-4") {
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
            script(src = "/share.js") {}
            div(classes = "modal-overlay hidden fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50") {
                div(classes = "modal-content bg-white rounded-lg shadow-lg p-6 relative max-w-md w-full") {
                    button(classes = "close-button absolute top-4 right-4") {
                        onClick = "closeShareModal()"
                        i(classes = "fas fa-times fa-lg") {} // FontAwesome close icon
                    }
                    div(classes = "share-preview") {
                        img(classes = "share-preview-image", src = "", alt = "")
                        h4(classes = "share-preview-title") { }
                    }
                    input(classes = "share-url-input mt-4 mb-2 w-full", type = InputType.text) {
                        readonly = true
                        value = "" // Will be set by JS
                    }
                    button(classes = "copy-button w-full") {
                        onClick = "copyShareLink(this)"
                        i(classes = "fas fa-copy mr-2") {}
                        +"Copy Link"
                    }
                }
            }
        }
    }
} 