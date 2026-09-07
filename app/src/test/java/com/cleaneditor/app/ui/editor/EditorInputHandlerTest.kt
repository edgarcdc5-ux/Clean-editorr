package com.cleaneditor.app.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class EditorInputHandlerTest {
    @Test
    fun openingBraceAddsClosingBraceAndKeepsCursorInside() {
        val result = EditorInputHandler.handle(TextFieldValue("", TextRange.Zero), TextFieldValue("{", TextRange(1)))
        assertEquals("{}", result.text)
        assertEquals(1, result.selection.start)
    }

    @Test
    fun openingQuoteAddsClosingQuote() {
        val result = EditorInputHandler.handle(TextFieldValue("", TextRange.Zero), TextFieldValue("\"", TextRange(1)))
        assertEquals("\"\"", result.text)
        assertEquals(1, result.selection.start)
    }

    @Test
    fun enterCopiesBaseIndentAndAddsIndentAfterBrace() {
        val old = TextFieldValue("    {", TextRange(5))
        val incoming = TextFieldValue("    {\n", TextRange(6))
        val result = EditorInputHandler.handle(old, incoming)
        assertEquals("    {\n        ", result.text)
        assertEquals(14, result.selection.start)
    }
}
