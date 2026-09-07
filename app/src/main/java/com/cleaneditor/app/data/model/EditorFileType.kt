package com.cleaneditor.app.data.model

/** Supported text-based document formats in CleanEditor. */
enum class EditorFileType(
    val displayName: String,
    val primaryExtension: String,
    val extensions: Set<String>,
    val category: Category,
    val languageName: String,
    val supportsStructuredFormatting: Boolean
) {
    TXT("Texto", "txt", setOf("txt"), Category.TEXT, "Texto", false),
    MARKDOWN("Markdown", "md", setOf("md"), Category.MARKUP, "Markdown", false),
    JSON("JSON", "json", setOf("json"), Category.DATA, "JSON", true),
    CSV("CSV", "csv", setOf("csv"), Category.DATA, "CSV", false),
    HTML("HTML", "html", setOf("html"), Category.WEB, "HTML", true),
    CSS("CSS", "css", setOf("css"), Category.WEB, "CSS", false),
    JAVASCRIPT("JavaScript", "js", setOf("js"), Category.CODE, "JavaScript", false),
    XML("XML", "xml", setOf("xml"), Category.MARKUP, "XML", true),
    YAML("YAML", "yaml", setOf("yaml", "yml"), Category.CONFIGURATION, "YAML", false),
    LOG("Log", "log", setOf("log"), Category.TEXT, "Log", false);

    enum class Category {
        TEXT,
        MARKUP,
        DATA,
        WEB,
        CODE,
        CONFIGURATION
    }

    companion object {
        fun fromFileName(fileName: String): EditorFileType {
            val extension = fileName.substringAfterLast('.', "").lowercase()
            return entries.firstOrNull { extension in it.extensions } ?: TXT
        }

        fun isSupportedExtension(extension: String): Boolean =
            entries.any { extension.trimStart('.').lowercase() in it.extensions }
    }
}
