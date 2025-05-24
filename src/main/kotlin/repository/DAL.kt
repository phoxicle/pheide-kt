package com.pheide.repository

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("Main")

object DAL {

    fun connect() {
        // Initialize the database connection
        Database.connect("jdbc:sqlite:data.db", driver = "org.sqlite.JDBC")
    }

    // Define schema and populate data
    fun createSchemaAndPopulateData() {
        logger.info("Creating schema")
        transaction {
            SchemaUtils.create(PageTable, TabTable)

            // Populate test data
            if (PageTable.selectAll().empty()) {
                logger.info("Populating test data")
                val insertedPageId = PageTable.insertAndGetId {
                    it[title] = "Home"
                    it[headerCssId] = "milk"
                    it[isDefault] = true
                }

                TabTable.insert {
                    it[pageId] = insertedPageId.value
                    it[title] = "Home"
                    it[aside] = "Aside content"
                    it[content] = "Main content"
                    it[sorting] = 1
                    it[type] = "text"
                }
            }
        }
    }
}

// Database table definitions
object PageTable : IntIdTable("page") {
    val title = varchar("title", 255)
    val headerCssId = varchar("header_css_id", 50)
    val isDefault = bool("is_default")
}

object TabTable : IntIdTable("tab") {
    val pageId = integer("page_id").references(PageTable.id)
    val title = varchar("title", 255)
    val aside = text("aside")
    val content = text("content")
    val sorting = integer("sorting")
    val type = varchar("type", 50)
}
