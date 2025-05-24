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
