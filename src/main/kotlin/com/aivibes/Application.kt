package com.aivibes

import com.aivibes.database.DatabaseFactory
import com.aivibes.routes.articleRoutes
import com.aivibes.services.ArticleService
import com.aivibes.templates.HomePage
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

@Serializable
data class HealthResponse(
    val status: String,
    val version: String,
    val timestamp: Long
)

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
    install(Compression)
    install(CachingHeaders)
    
    routing {
        staticResources("/static", "static")
        
        get("/favicon.svg") {
            val resource = javaClass.classLoader.getResource("static/favicon.svg")
                ?: throw Exception("Favicon not found")
            call.respondFile(File(resource.file))
        }

        get("/health") {
            call.respond(
                HealthResponse(
                    status = "healthy",
                    version = "1.0.0",
                    timestamp = System.currentTimeMillis()
                )
            )
            logger.info { "Received request for health check" }
        }
        
        get("/") {
            logger.info { "Received request for root path" }
            try {
                val articles = runBlocking { articleService.fetchLatestArticles() }
                logger.info { "Fetched ${articles.size} articles" }
                call.respondHtmlTemplate(HomePage(articles)) {}
            } catch (e: Exception) {
                logger.error(e) { "Error handling root path" }
                throw e
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