package com.pheide.repository

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.text.get

class PageRepository {

    fun selectAll(): List<Page> {
        return transaction {
            PageTable.selectAll()
                .map { Page(it) }
        }
    }

    fun selectById(id: Int): Page? {
        return transaction {
            PageTable.selectAll()
                .where { PageTable.id eq id }
                .map { Page(it) }
                .firstOrNull()
        }
    }

    fun selectByTitle(title: String): Page? {
        return transaction {
            PageTable.selectAll()
                .where { PageTable.title.lowerCase() eq title.lowercase() }
                .map { Page(it) }
                .firstOrNull()
        }
    }

    fun selectDefault(): Page? {
        return transaction {
            PageTable.selectAll()
                .where { PageTable.isDefault eq true }
                .map { Page(it) }
                .firstOrNull()
        }
    }

    fun selectRandom(): Page? {
        return transaction {
            PageTable.selectAll()
                .map { Page(it) }
                .firstOrNull()
        }
    }

    fun create(title: String, headerCssId: String, isDefault: Boolean): Int {
        return transaction {
            val insertedId = PageTable.insert {
                it[PageTable.title] = title
                it[PageTable.headerCssId] = headerCssId
                it[PageTable.isDefault] = isDefault
            } get PageTable.id
            insertedId.value
        }
    }

    fun update(pageId: Int, title: String? = null, isDefault: Boolean? = null) {
        transaction {
            PageTable.update({ PageTable.id eq pageId }) {
                if (title != null) it[PageTable.title] = title
                if (isDefault != null) it[PageTable.isDefault] = isDefault
            }
        }
    }

    fun delete(pageId: Int) {
        transaction {
            PageTable.deleteWhere {
                PageTable.id eq pageId
            }
        }
    }
}

data class Page(
    val id: Int,
    val title: String,
    val headerCssId: String,
    val isDefault: Boolean
) {
    constructor(row: ResultRow) : this(
        id = row[PageTable.id].value,
        title = row[PageTable.title],
        headerCssId = row[PageTable.headerCssId],
        isDefault = row[PageTable.isDefault]
    )
}
