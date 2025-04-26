package com.aivibes.routes

import com.aivibes.database.DatabaseFactory
import com.aivibes.models.Article
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.articleRoutes(database: DatabaseFactory) {
    route("/articles") {
        get {
            val articles = database.getAllArticles()
            call.respond(articles)
        }

        post {
            val article = call.receive<Article>()
            database.createArticle(article)
            call.respond(HttpStatusCode.Created, article)
        }
    }
} 