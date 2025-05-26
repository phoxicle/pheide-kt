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

    fun selectDefault(): Page? {
        return transaction {
            PageTable.selectAll()
                .where { PageTable.isDefault eq true }
                .map { Page(it) }
                .firstOrNull()
        }
    }

    fun update(pageId: Int, title: String? = null) {
        transaction {
            PageTable.update({ PageTable.id eq pageId }) {
                if (title != null) it[TabTable.title] = title
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
