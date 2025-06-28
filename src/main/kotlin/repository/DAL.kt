package com.pheide.repository

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("DAL")

object DAL {

    fun connect() {
        // Initialize the database connection
        val dbPath = System.getenv("DB_NAME") ?: "data.db"
        logger.info("Using DB: $dbPath")
        Database.connect("jdbc:sqlite:$dbPath", driver = "org.sqlite.JDBC")
    }

    fun clearData() {
        logger.info("Clearing data")
        transaction {
            TabTable.deleteAll()
            PageTable.deleteAll()
        }
    }

    // Define schema and populate test data
    fun createSchemaAndPopulateDataIfNone() {
        logger.info("Creating schema")
        transaction {
            SchemaUtils.create(PageTable, TabTable)

            // Populate test data
            if (PageTable.selectAll().empty()) {
                logger.info("Populating test data")
                val milkPageId = PageTable.insertAndGetId {
                    it[title] = "Home"
                    it[headerCssId] = "milk"
                    it[isDefault] = true
                }

                TabTable.insert {
                    it[pageId] = milkPageId.value
                    it[title] = "Home tab 1"
                    it[aside] = "Aside content 1"
                    it[content] = "Main content 1"
                    it[sorting] = 1
                    it[type] = "text"
                }

                TabTable.insert {
                    it[pageId] = milkPageId.value
                    it[title] = "Home tab 2"
                    it[aside] = "Aside content 2"
                    it[content] = "Main content 2"
                    it[sorting] = 10
                    it[type] = "text"
                }

                val millPageId = PageTable.insertAndGetId {
                    it[title] = "Hobby"
                    it[headerCssId] = "mill"
                    it[isDefault] = true
                }

                TabTable.insert {
                    it[pageId] = millPageId.value
                    it[title] = "Hobby tab 1"
                    it[aside] = "mill tab aside 1"
                    it[content] = "mill tab content 1"
                    it[sorting] = 5
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
