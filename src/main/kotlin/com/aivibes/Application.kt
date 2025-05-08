package com.aivibes

import com.aivibes.database.DatabaseFactory
import com.aivibes.database.ShareCounts
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
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger {}

@Serializable
data class HealthResponse(
    val status: String,
    val version: String,
    val timestamp: Long
)

@Serializable
data class ShareCountRequest(val articleId: String, val platform: String)

@Serializable
data class ShareCountResponse(val articleId: String, val platform: String, val count: Int)

fun main() {
    val database = DatabaseFactory()
    database.init()
    val articleService = ArticleService()
    
    embeddedServer(Netty, port = 8080) {
        configureSerialization()
        configureRouting(database, articleService)
        shareCountRoutes()
    }.start(wait = true)
}

fun Application.configureRouting(database: DatabaseFactory, articleService: ArticleService) {
    install(Compression)
    install(CachingHeaders)
    
    routing {
        static("/") {
            resources("static")
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

fun Application.shareCountRoutes() {
    routing {
        get("/api/share-counts") {
            val counts = transaction {
                ShareCounts.selectAll().map {
                    "${it[ShareCounts.articleId]}:${it[ShareCounts.platform]}" to it[ShareCounts.count]
                }.toMap()
            }
            call.respond(counts)
        }
        
        post("/api/share") {
            val req = call.receive<ShareCountRequest>()
            val response = transaction {
                val existingCount = ShareCounts.select {
                    (ShareCounts.articleId eq req.articleId) and (ShareCounts.platform eq req.platform)
                }.singleOrNull()

                if (existingCount != null) {
                    // Update existing count
                    val newCount = existingCount[ShareCounts.count] + 1
                    ShareCounts.update({ 
                        (ShareCounts.articleId eq req.articleId) and (ShareCounts.platform eq req.platform)
                    }) {
                        it[count] = newCount
                    }
                    ShareCountResponse(req.articleId, req.platform, newCount)
                } else {
                    // Insert new count
                    ShareCounts.insert {
                        it[articleId] = req.articleId
                        it[platform] = req.platform
                        it[count] = 1
                    }
                    ShareCountResponse(req.articleId, req.platform, 1)
                }
            }
            call.respond(response)
        }
    }
} 