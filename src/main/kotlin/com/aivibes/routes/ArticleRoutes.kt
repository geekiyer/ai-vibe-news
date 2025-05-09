package com.aivibes.routes

import com.aivibes.database.DatabaseFactory
import com.aivibes.models.Article
import com.aivibes.templates.ArticlePreviewPage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

@Serializable
data class ClickRecord(
    val articleId: String,
    val referrer: String? = null,
    val platform: String? = null
)

fun Route.articleRoutes(database: DatabaseFactory) {
    route("/articles") {
        get {
            val articles = runBlocking { database.getAllArticles() }
            call.respond(articles)
        }

        post {
            val article = call.receive<Article>()
            runBlocking { database.createArticle(article) }
            call.respond(HttpStatusCode.Created, article)
        }
    }

    // Short URL route
    get("/a/{id}") {
        val articleId = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.NotFound)
        val article = runBlocking { database.getArticleById(articleId) } 
            ?: return@get call.respond(HttpStatusCode.NotFound)
        
        call.respondHtmlTemplate(ArticlePreviewPage(article)) {}
    }
    
    // Click tracking endpoint
    post("/api/record-click") {
        val click = call.receive<ClickRecord>()
        val userAgent = call.request.headers["User-Agent"]
        val ipAddress = call.request.origin.remoteHost
        // Maybe use a GeoIP service here
        val country = null 
        
        runBlocking {
            database.recordClick(
                articleId = click.articleId,
                referrer = click.referrer,
                platform = click.platform,
                userAgent = userAgent,
                ipAddress = ipAddress,
                country = country
            )
        }
        
        call.respond(HttpStatusCode.OK)
    }
    
    // Analytics endpoint (optional, for admin dashboard)
    get("/api/analytics/{articleId}") {
        val articleId = call.parameters["articleId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val stats = runBlocking { database.getClickStats(articleId) }
        call.respond(stats)
    }
} 