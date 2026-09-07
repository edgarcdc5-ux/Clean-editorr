package com.cleaneditor.app.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/** Line-level editing operations for the advanced editor toolbar. */
object EditorLineOperations {
    private const val INDENT = "    "

    fun indent(value: TextFieldValue): TextFieldValue = transformSelectedLines(value) { INDENT + it }

    fun outdent(value: TextFieldValue): TextFieldValue = transformSelectedLines(value) { line ->
        when {
            line.startsWith(INDENT) -> line.removePrefix(INDENT)
            line.startsWith("\t") -> line.removePrefix("\t")
            line.startsWith(" ") -> line.removePrefix(" ")
            else -> line
        }
    }

    fun selectCurrentLine(value: TextFieldValue): TextFieldValue {
        val start = value.selection.min
        val end = value.selection.max
        val lineStart = value.text.lastIndexOf('\n', (start - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
        val lineEndIndex = value.text.indexOf('\n', end.coerceAtMost(value.text.length))
        val lineEnd = if (lineEndIndex < 0) value.text.length else lineEndIndex
        return value.copy(selection = TextRange(lineStart, lineEnd))
    }

    fun duplicateCurrentLine(value: TextFieldValue): TextFieldValue {
        val cursor = value.selection.min
        val lineStart = value.text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
        val lineEndIndex = value.text.indexOf('\n', cursor.coerceAtMost(value.text.length))
        val lineEnd = if (lineEndIndex < 0) value.text.length else lineEndIndex
        val line = value.text.substring(lineStart, lineEnd)
        val insertion = "\n$line"
        val text = value.text.substring(0, lineEnd) + insertion + value.text.substring(lineEnd)
        val selectionStart = value.selection.start + if (value.selection.start >= lineEnd) insertion.length else 0
        val selectionEnd = value.selection.end + if (value.selection.end >= lineEnd) insertion.length else 0
        return value.copy(text = text, selection = TextRange(selectionStart, selectionEnd))
    }

    private fun transformSelectedLines(value: TextFieldValue, transform: (String) -> String): TextFieldValue {
        val selectionStart = value.selection.min
        val selectionEnd = value.selection.max
        val lineStart = value.text.lastIndexOf('\n', (selectionStart - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
        val lineEndIndex = value.text.indexOf('\n', selectionEnd.coerceAtMost(value.text.length))
        val lineEnd = if (lineEndIndex < 0) value.text.length else lineEndIndex
        val block = value.text.substring(lineStart, lineEnd)
        val transformed = block.split('\n').joinToString("\n", transform = transform)
        if (transformed == block) return value
        val text = value.text.substring(0, lineStart) + transformed + value.text.substring(lineEnd)
        val delta = transformed.length - block.length
        val newStart = (value.selection.start + (if (value.selection.start >= lineStart) minOf(delta, value.selection.start - lineStart + delta) else 0)).coerceIn(0, text.length)
        val newEnd = (value.selection.end + delta).coerceIn(newStart, text.length)
        return value.copy(text = text, selection = TextRange(newStart, newEnd))
    }
}
