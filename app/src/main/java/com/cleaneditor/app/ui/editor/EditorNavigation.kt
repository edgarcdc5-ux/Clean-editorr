package com.cleaneditor.app.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/** Keyboard navigation and selection helpers for the editor. */
object EditorNavigation {
    fun moveLineStart(value: TextFieldValue, select: Boolean = false): TextFieldValue = moveTo(value, lineStart(value.text, value.selection.start), select)

    fun moveLineEnd(value: TextFieldValue, select: Boolean = false): TextFieldValue = moveTo(value, lineEnd(value.text, value.selection.end), select)

    fun moveDocumentStart(value: TextFieldValue, select: Boolean = false): TextFieldValue = moveTo(value, 0, select)

    fun moveDocumentEnd(value: TextFieldValue, select: Boolean = false): TextFieldValue = moveTo(value, value.text.length, select)

    fun selectCurrentLine(value: TextFieldValue): TextFieldValue {
        val start = lineStart(value.text, value.selection.start)
        val end = lineEnd(value.text, value.selection.end)
        return value.copy(selection = TextRange(start, end))
    }

    private fun moveTo(value: TextFieldValue, position: Int, select: Boolean): TextFieldValue {
        val target = position.coerceIn(0, value.text.length)
        val selection = if (select) TextRange(value.selection.start, target) else TextRange(target)
        return value.copy(selection = selection)
    }

    private fun lineStart(text: String, position: Int): Int {
        val cursor = position.coerceIn(0, text.length)
        val breakIndex = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0))
        return if (breakIndex < 0) 0 else breakIndex + 1
    }

    private fun lineEnd(text: String, position: Int): Int {
        val cursor = position.coerceIn(0, text.length)
        val breakIndex = text.indexOf('\n', cursor)
        return if (breakIndex < 0) text.length else breakIndex
    }
}
