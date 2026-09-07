package com.cleaneditor.app.ui.editor

import com.cleaneditor.app.data.model.EditorFileType

/** Small, deterministic formatting helpers used by the editor. */
object EditorFormatter {
    fun format(content: String, type: EditorFileType): String = when (type) {
        EditorFileType.JSON -> formatDelimited(content, '{', '}', '[', ']', ',')
        EditorFileType.HTML, EditorFileType.XML -> formatMarkup(content)
        else -> content
    }

    private fun formatDelimited(
        content: String,
        openObject: Char,
        closeObject: Char,
        openArray: Char,
        closeArray: Char,
        separator: Char
    ): String {
        val compact = content.trim()
        if (compact.isEmpty()) return content
        val result = StringBuilder()
        var indent = 0
        var inString = false
        var escaped = false
        var pendingSpace = false

        fun newline() {
            while (result.isNotEmpty() && result.last() == ' ') result.deleteCharAt(result.lastIndex)
            result.append('\n')
            repeat(indent) { result.append("    ") }
        }

        for (char in compact) {
            if (inString) {
                result.append(char)
                if (escaped) escaped = false
                else if (char == '\\') escaped = true
                else if (char == '"') inString = false
                continue
            }
            when (char) {
                '"' -> { inString = true; result.append(char); pendingSpace = false }
                openObject, openArray -> {
                    result.append(char)
                    indent++
                    newline()
                    pendingSpace = false
                }
                closeObject, closeArray -> {
                    indent = (indent - 1).coerceAtLeast(0)
                    newline()
                    result.append(char)
                    pendingSpace = false
                }
                separator -> {
                    result.append(char)
                    newline()
                    pendingSpace = false
                }
                ':' -> {
                    result.append(": ")
                    pendingSpace = false
                }
                '\n', '\r', '\t', ' ' -> pendingSpace = true
                else -> {
                    if (pendingSpace && result.isNotEmpty() && result.last() != '\n' && result.last() != ' ') result.append(' ')
                    result.append(char)
                    pendingSpace = false
                }
            }
        }
        return result.toString().trimEnd()
    }

    private fun formatMarkup(content: String): String {
        val compact = content.replace(Regex(">\\s+<"), "><").trim()
        if (compact.isEmpty()) return content
        val tokens = Regex("<[^>]+>|[^<]+(?=<)").findAll(compact).map { it.value.trim() }.filter { it.isNotEmpty() }.toList()
        if (tokens.isEmpty()) return content
        val result = StringBuilder()
        var indent = 0
        for (token in tokens) {
            val closing = token.startsWith("</")
            val selfClosing = token.endsWith("/>") || token.startsWith("<!") || token.startsWith("<?")
            if (closing) indent = (indent - 1).coerceAtLeast(0)
            if (result.isNotEmpty()) result.append('\n')
            repeat(indent) { result.append("    ") }
            result.append(token)
            if (token.startsWith("<") && !closing && !selfClosing && !token.contains("</")) indent++
        }
        return result.toString()
    }
}
