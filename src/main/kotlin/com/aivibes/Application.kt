package com.aivibes

import com.aivibes.database.DatabaseFactory
import com.aivibes.routes.articleRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.html.*
import kotlinx.html.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun main() {
    val database = DatabaseFactory()
    database.init()

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting(database)
        configureSerialization()
    }.start(wait = true)
}

fun Application.configureRouting(database: DatabaseFactory) {
    routing {
        get("/") {
            call.respondHtml {
                head {
                    title { +"AI Vibe News" }
                    style {
                        unsafe {
                            +"""
                            body {
                                font-family: 'Arial', sans-serif;
                                margin: 0;
                                padding: 0;
                                background-color: #f5f5f5;
                            }
                            .container {
                                max-width: 1200px;
                                margin: 0 auto;
                                padding: 20px;
                            }
                            .header {
                                background-color: #2c3e50;
                                color: white;
                                padding: 20px;
                                text-align: center;
                            }
                            .article-card {
                                background-color: white;
                                border-radius: 8px;
                                padding: 20px;
                                margin: 20px 0;
                                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                            }
                            .article-title {
                                color: #2c3e50;
                                font-size: 24px;
                                margin-bottom: 10px;
                            }
                            .article-date {
                                color: #7f8c8d;
                                font-size: 14px;
                            }
                            .article-content {
                                color: #34495e;
                                line-height: 1.6;
                            }
                            .tag {
                                display: inline-block;
                                background-color: #3498db;
                                color: white;
                                padding: 4px 8px;
                                border-radius: 4px;
                                margin-right: 8px;
                                font-size: 12px;
                            }
                            @media (max-width: 768px) {
                                .container {
                                    padding: 10px;
                                }
                                .article-card {
                                    padding: 15px;
                                }
                            }
                            """
                        }
                    }
                }
                body {
                    div("header") {
                        h1 { +"AI Vibe News" }
                        p { +"Daily Insights into AI and Vibe Coding" }
                    }
                    div("container") {
                        div("article-card") {
                            h2("article-title") { +"Welcome to AI Vibe News" }
                            p("article-date") { +"April 26, 2024" }
                            div("article-content") {
                                p { +"Stay tuned for daily articles about AI and Vibe coding. Our mission is to educate and inspire developers around the world." }
                            }
                            div {
                                span("tag") { +"AI" }
                                span("tag") { +"Vibe Coding" }
                                span("tag") { +"Education" }
                            }
                        }
                    }
                }
            }
        }
        articleRoutes(database)
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
} 