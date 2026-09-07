package com.cleaneditor.app.ui.editor

import com.cleaneditor.app.data.model.EditorFileType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorFormatterTest {
    @Test
    fun jsonGetsReadableIndentation() {
        val formatted = EditorFormatter.format("{\"name\":\"CleanEditor\",\"items\":[1,2]}", EditorFileType.JSON)
        assertTrue(formatted.contains("\n    \"name\": \"CleanEditor\""))
        assertTrue(formatted.contains("\n        1,"))
        assertTrue(formatted.contains("\n    ]"))
    }

    @Test
    fun markupGetsBasicIndentation() {
        val formatted = EditorFormatter.format("<div><p>Hello</p></div>", EditorFileType.HTML)
        assertEquals("<div>\n    <p>Hello</p>\n</div>", formatted)
    }

    @Test
    fun plainTextIsPreserved() {
        val content = "linha 1\nlinha 2"
        assertEquals(content, EditorFormatter.format(content, EditorFileType.TXT))
    }
}
