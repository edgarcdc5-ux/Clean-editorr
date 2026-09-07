package com.cleaneditor.app.data.model

/** Supported text-based document formats in CleanEditor. */
enum class EditorFileType(
    val displayName: String,
    val primaryExtension: String,
    val extensions: Set<String>
) {
    TXT("Texto", "txt", setOf("txt")),
    MARKDOWN("Markdown", "md", setOf("md")),
    JSON("JSON", "json", setOf("json")),
    CSV("CSV", "csv", setOf("csv")),
    HTML("HTML", "html", setOf("html")),
    CSS("CSS", "css", setOf("css")),
    JAVASCRIPT("JavaScript", "js", setOf("js")),
    XML("XML", "xml", setOf("xml")),
    YAML("YAML", "yaml", setOf("yaml", "yml")),
    LOG("Log", "log", setOf("log"));

    companion object {
        fun fromFileName(fileName: String): EditorFileType {
            val extension = fileName.substringAfterLast('.', "").lowercase()
            return entries.firstOrNull { extension in it.extensions } ?: TXT
        }

        fun isSupportedExtension(extension: String): Boolean =
            entries.any { extension.trimStart('.').lowercase() in it.extensions }
    }
}
