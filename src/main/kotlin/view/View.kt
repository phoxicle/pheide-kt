package com.pheide.view

import org.slf4j.LoggerFactory

open class View(private val templateName: String, val vars: MutableMap<String, String> = mutableMapOf()) {
    private val logger = LoggerFactory.getLogger("View")

    fun render(additionalVars: Map<String, String> = emptyMap()): String {
        return replaceTemplateVars(readFile(templateName), vars + additionalVars)
    }

    fun renderIf(condition: Boolean) : String {
        return if (condition) render() else ""
    }

    fun renderPage(additionalVars: Map<String, String> = emptyMap()): String {
        val body = readFile(templateName)
        val header = readFile("header.html")
        val footer = readFile("footer.html")

        var content = header + body + footer
        content = replaceTemplateVars(content, vars + additionalVars)
        return content
    }

    private fun replaceTemplateVars(content: String, templateVars: Map<String, String>): String {
        var result = content
        for ((key, value) in templateVars) {
            result = result.replace("%%%$key%%%", value)
        }
        return result
    }

    private fun readFile(templateName: String): String {
        // Use View::class.java to get the class loader
        val resourceStream = View::class.java.classLoader.getResourceAsStream("templates/$templateName")
            ?: throw Exception("Template file not found: $templateName")

        return resourceStream.bufferedReader().use { it.readText() }
    }
}
