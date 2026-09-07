package com.cleaneditor.app.ui.editor

import com.cleaneditor.app.data.model.EditorFileType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyntaxHighlighterTest {
    @Test
    fun jsonKeepsTextAndAddsStyles() {
        val source = "{\"name\":\"CleanEditor\",\"count\":42,\"ok\":true}"
        val result = SyntaxHighlighter.highlight(source, EditorFileType.JSON)
        assertEquals(source, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun javascriptHighlightsKeywordsStringsNumbersAndComments() {
        val source = "const name = 'CleanEditor'; // editor\nreturn 42"
        val result = SyntaxHighlighter.highlight(source, EditorFileType.JAVASCRIPT)
        assertEquals(source, result.text)
        assertTrue(result.spanStyles.size >= 4)
    }

    @Test
    fun markupHighlightsTagsAndAttributes() {
        val source = "<div class=\"editor\">Hello</div>"
        val result = SyntaxHighlighter.highlight(source, EditorFileType.HTML)
        assertEquals(source, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun plainTextRemainsUnstyled() {
        val source = "linha 1\nlinha 2"
        val result = SyntaxHighlighter.highlight(source, EditorFileType.TXT)
        assertEquals(source, result.text)
        assertTrue(result.spanStyles.isEmpty())
    }
}
