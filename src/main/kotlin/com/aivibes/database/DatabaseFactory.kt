package com.aivibes.database

import com.aivibes.models.Article
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object Articles : Table() {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val content = text("content")
    val author = varchar("author", 100)
    val publishDate = varchar("publish_date", 50)
    val tags = varchar("tags", 255)
    val imageUrl = varchar("image_url", 255).nullable()
    val articleSource = varchar("source", 50)

    override val primaryKey = PrimaryKey(id)
}

object ShareCounts : Table() {
    val id = integer("id").autoIncrement()
    val articleId = varchar("article_id", 255)
    val platform = varchar("platform", 50)
    val count = integer("count").default(0)
    
    override val primaryKey = PrimaryKey(id)
    
    init {
        uniqueIndex(articleId, platform) // Ensure one count per article-platform combination
    }
}

class DatabaseFactory {
    fun init() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Articles)
            SchemaUtils.create(ShareCounts)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun createArticle(article: Article) = dbQuery {
        Articles.insert {
            it[title] = article.title
            it[content] = article.content
            it[author] = article.author
            it[publishDate] = article.publishedAt
            it[tags] = article.tags.joinToString(",")
            it[imageUrl] = article.imageUrl
        }
    }

    suspend fun getAllArticles(): List<Article> = dbQuery {
        Articles.selectAll().map {
            Article(
                id = it[Articles.id],
                title = it[Articles.title],
                content = it[Articles.content],
                author = it[Articles.author],
                publishedAt = it[Articles.publishDate],
                tags = it[Articles.tags].split(","),
                imageUrl = it[Articles.imageUrl],
                source = it[Articles.articleSource]
            )
        }
    }
} 