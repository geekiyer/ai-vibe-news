package com.aivibes

import com.aivibes.database.DatabaseFactory
import com.aivibes.routes.articleRoutes
import com.aivibes.services.ArticleService
import com.aivibes.templates.HomePage
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

fun main() {
    val database = DatabaseFactory()
    database.init()
    val articleService = ArticleService()
    
    embeddedServer(Netty, port = 8080) {
        configureSerialization()
        configureRouting(database, articleService)
    }.start(wait = true)
}

fun Application.configureRouting(database: DatabaseFactory, articleService: ArticleService) {
    routing {
        get("/") {
            val articles = runBlocking { articleService.fetchLatestArticles() }
            call.respondHtmlTemplate(HomePage(articles)) {}
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