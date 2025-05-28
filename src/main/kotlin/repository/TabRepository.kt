package com.pheide.repository

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class TabRepository {

    fun selectById(id: Int): Tab? {
        return transaction {
            TabTable.selectAll()
                .where { TabTable.id eq id }
                .map { Tab(it) }
                .firstOrNull()
        }
    }

    fun selectByPageIdAndTitle(pageId: Int, title: String): Tab? {
        return transaction {
            TabTable.selectAll()
                .where {
                    TabTable.pageId eq pageId
                    TabTable.title.lowerCase() eq title.lowercase()
                }
                .map { Tab(it) }
                .firstOrNull()
        }
    }

    fun selectDefault(pageId: Int): Tab? {
        return transaction {
            TabTable.selectAll()
                .where { TabTable.pageId eq pageId }
                .orderBy(TabTable.sorting)
                .map { Tab(it) }
                .firstOrNull()
        }
    }

    fun selectAllByPageId(pageId: Int): List<Tab> {
        return transaction {
            TabTable.selectAll()
                .where { TabTable.pageId eq pageId }
                .orderBy(TabTable.sorting)
                .map { Tab(it) }
        }
    }

    fun update(tabId: Int, title: String? = null, content: String? = null, aside: String? = null, sorting: Int? = null) {
        transaction {
            TabTable.update({ TabTable.id eq tabId }) {
                if (content != null) it[TabTable.content] = content
                if (aside != null) it[TabTable.aside] = aside
                if (title != null) it[TabTable.title] = title
                if (sorting != null) it[TabTable.sorting] = sorting
            }
        }
    }

    fun create(pageId: Int, title: String): Int {
        return transaction {
            val insertedId = TabTable.insert {
                it[TabTable.pageId] = pageId
                it[TabTable.title] = title
                it[TabTable.aside] = ""
                it[TabTable.content] = ""
                it[TabTable.sorting] = 1 // TODO
                it[TabTable.type] = "text"
            } get TabTable.id
            insertedId.value
        }
    }

    fun delete(tabId: Int) {
        transaction {
            TabTable.deleteWhere {
                TabTable.id eq tabId
            }
        }
    }

    fun deleteAllByPageId(pageId: Int) {
        transaction {
            TabTable.deleteWhere {
                TabTable.pageId eq pageId
            }
        }
    }
}

data class Tab(
    val id: Int,
    val pageId: Int,
    val title: String,
    val aside: String,
    val content: String,
    val sorting: Int,
) {
    constructor(row: ResultRow) : this(
        id = row[TabTable.id].value,
        pageId = row[TabTable.pageId],
        title = row[TabTable.title],
        aside = row[TabTable.aside],
        content = row[TabTable.content],
        sorting = row[TabTable.sorting],
    )
}
