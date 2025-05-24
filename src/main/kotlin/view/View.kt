package com.pheide.view

class View(val vars: MutableMap<String, String> = mutableMapOf()) {

    fun renderPage(templateName: String): String {
        val body = readFile(templateName)
        val header = readFile("header.html")
        val footer = readFile("footer.html")

        var content = header + body + footer
        content = replaceTemplateVars(content, vars)
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
