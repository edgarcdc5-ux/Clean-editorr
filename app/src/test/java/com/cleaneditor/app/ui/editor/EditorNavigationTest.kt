package com.cleaneditor.app.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class EditorNavigationTest {
    @Test
    fun movesToLineStartAndEnd() {
        val value = TextFieldValue("abc\ndef", TextRange(5))
        assertEquals(4, EditorNavigation.moveLineStart(value).selection.start)
        assertEquals(7, EditorNavigation.moveLineEnd(value).selection.start)
    }

    @Test
    fun movesToDocumentStartAndEnd() {
        val value = TextFieldValue("abc\ndef", TextRange(5))
        assertEquals(0, EditorNavigation.moveDocumentStart(value).selection.start)
        assertEquals(7, EditorNavigation.moveDocumentEnd(value).selection.start)
    }

    @Test
    fun shiftNavigationExtendsSelection() {
        val value = TextFieldValue("abc\ndef", TextRange(5))
        val result = EditorNavigation.moveLineStart(value, select = true)
        assertEquals(4, result.selection.min)
        assertEquals(5, result.selection.max)
    }

    @Test
    fun selectsCurrentLineWithoutNewline() {
        val value = TextFieldValue("abc\ndef", TextRange(5))
        val result = EditorNavigation.selectCurrentLine(value)
        assertEquals(TextRange(4, 7), result.selection)
    }
}
