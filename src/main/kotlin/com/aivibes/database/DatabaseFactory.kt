package com.aivibes.database

import com.aivibes.models.Article
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object Articles : Table() {
    val id = integer("id")
    val title = varchar("title", 255)
    val content = text("content")
    val author = varchar("author", 100)
    val publishDate = varchar("publish_date", 50)
    val tags = varchar("tags", 255)
    val imageUrl = varchar("image_url", 255).nullable()
    val articleSource = varchar("source", 50)
    val url = varchar("url", 255).nullable()

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

object LinkClicks : Table() {
    val id = integer("id").autoIncrement()
    val articleId = varchar("article_id", 255)
    val timestamp = datetime("timestamp").default(LocalDateTime.now())
    val referrer = varchar("referrer", 255).nullable()
    val platform = varchar("platform", 50).nullable() // twitter, facebook, linkedin, etc.
    val userAgent = text("user_agent").nullable()
    val ipAddress = varchar("ip_address", 45).nullable() // IPv6 compatible
    val country = varchar("country", 2).nullable()
    
    override val primaryKey = PrimaryKey(id)
    
    init {
        index(true, articleId, timestamp) // Index for quick lookups
    }
}

class DatabaseFactory {
    fun init() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Articles)
            SchemaUtils.create(ShareCounts)
            SchemaUtils.create(LinkClicks)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun createArticle(article: Article) = dbQuery {
        Articles.insert {
            it[id] = article.id
            it[title] = article.title
            it[content] = article.content
            it[author] = article.author
            it[publishDate] = article.publishedAt
            it[tags] = article.tags.joinToString(",")
            it[imageUrl] = article.imageUrl
            it[articleSource] = article.source
            it[url] = article.url
        }
    }

    suspend fun getArticleById(id: String): Article? = dbQuery {
        Articles.select { Articles.id eq (id.toIntOrNull() ?: return@dbQuery null) }
            .singleOrNull()
            ?.let {
                Article(
                    id = it[Articles.id],
                    title = it[Articles.title],
                    content = it[Articles.content],
                    author = it[Articles.author],
                    publishedAt = it[Articles.publishDate],
                    tags = it[Articles.tags].split(","),
                    imageUrl = it[Articles.imageUrl],
                    source = it[Articles.articleSource],
                    url = it[Articles.url]
                )
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
                source = it[Articles.articleSource],
                url = it[Articles.url]
            )
        }
    }

    suspend fun recordClick(
        articleId: String,
        referrer: String?,
        platform: String?,
        userAgent: String?,
        ipAddress: String?,
        country: String?
    ) = dbQuery {
        LinkClicks.insert {
            it[LinkClicks.articleId] = articleId
            it[LinkClicks.referrer] = referrer
            it[LinkClicks.platform] = platform
            it[LinkClicks.userAgent] = userAgent
            it[LinkClicks.ipAddress] = ipAddress
            it[LinkClicks.country] = country
        }
    }

    suspend fun getClickStats(articleId: String): Map<String, Int> = dbQuery {
        val total = LinkClicks.select { LinkClicks.articleId eq articleId }.count()
        val byPlatform = LinkClicks
            .slice(LinkClicks.platform, LinkClicks.articleId)
            .select { LinkClicks.articleId eq articleId }
            .groupBy(LinkClicks.platform)
            .associate { it[LinkClicks.platform] to it[LinkClicks.id.count()] }

        buildMap {
            put("total", total.toInt())
            putAll(byPlatform.filterKeys { it != null }.mapKeys { it.key!!.toString() }.mapValues { it.value.toInt() })
        }
    }
} 