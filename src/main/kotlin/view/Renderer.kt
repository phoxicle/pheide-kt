package com.pheide.view

class Renderer {


    fun renderPage(templateName: String, templateVars: Map<String, String>): String {
        val body = readFile(templateName)

        val header = readFile("header.html")
        val footer = readFile("footer.html")
        val content = header + body + footer

        return content

    }

    private fun readFile(templateName: String): String {
        val filePath = javaClass.classLoader.getResource("templates/$templateName")?.path
        val file = java.io.File(filePath)
        if (file.exists()) {
            return file.readText()
        } else {
            throw Exception("File not found: $filePath")
        }
    }
}