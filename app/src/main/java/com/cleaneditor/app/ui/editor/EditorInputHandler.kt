package com.cleaneditor.app.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/** Small, dependency-free editing helpers for code-like documents. */
object EditorInputHandler {
    private val pairs = mapOf('(' to ')', '[' to ']', '{' to '}', '"' to '"', '\'' to '\'')

    fun handle(old: TextFieldValue, incoming: TextFieldValue): TextFieldValue {
        if (incoming.text == old.text) return incoming
        val start = old.selection.min
        val end = old.selection.max
        val removed = end - start
        val insertedLength = incoming.text.length - old.text.length + removed
        val inserted = if (insertedLength in 0..incoming.text.length && start <= incoming.text.length) {
            incoming.text.substring(start, start + insertedLength)
        } else ""

        if (inserted.length == 1 && incoming.selection.collapsed) {
            val ch = inserted[0]
            val close = pairs[ch]
            if (close != null) {
                val replacement = incoming.text.substring(0, start) + ch + close + incoming.text.substring(start + 1)
                return incoming.copy(text = replacement, selection = TextRange(start + 1))
            }
            if (ch in ")]}'\"" && start < incoming.text.length - 1 && incoming.text[start + 1] == ch) {
                return incoming.copy(selection = TextRange(start + 1))
            }
        }

        if (inserted == "\n" && incoming.selection.collapsed) {
            val cursor = incoming.selection.start
            val lineStart = incoming.text.lastIndexOf('\n', cursor - 2).let { if (it < 0) 0 else it + 1 }
            val previousLine = incoming.text.substring(lineStart, cursor - 1)
            val baseIndent = previousLine.takeWhile { it == ' ' || it == '\t' }
            val trimmed = previousLine.trimEnd()
            val extra = if (trimmed.endsWith("{") || trimmed.endsWith("[") || trimmed.endsWith("(")) "    " else ""
            val indent = baseIndent + extra
            val text = incoming.text.substring(0, cursor) + indent + incoming.text.substring(cursor)
            return incoming.copy(text = text, selection = TextRange(cursor + indent.length))
        }

        return incoming
    }
}
