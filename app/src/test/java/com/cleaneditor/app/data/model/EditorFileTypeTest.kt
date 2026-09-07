package com.cleaneditor.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorFileTypeTest {
    @Test
    fun detectsAllSupportedExtensions() {
        assertEquals(EditorFileType.TXT, EditorFileType.fromFileName("notes.txt"))
        assertEquals(EditorFileType.MARKDOWN, EditorFileType.fromFileName("README.md"))
        assertEquals(EditorFileType.JSON, EditorFileType.fromFileName("data.JSON"))
        assertEquals(EditorFileType.CSV, EditorFileType.fromFileName("table.csv"))
        assertEquals(EditorFileType.HTML, EditorFileType.fromFileName("index.html"))
        assertEquals(EditorFileType.CSS, EditorFileType.fromFileName("styles.css"))
        assertEquals(EditorFileType.JAVASCRIPT, EditorFileType.fromFileName("app.Js"))
        assertEquals(EditorFileType.XML, EditorFileType.fromFileName("layout.xml"))
        assertEquals(EditorFileType.YAML, EditorFileType.fromFileName("config.yaml"))
        assertEquals(EditorFileType.YAML, EditorFileType.fromFileName("config.yml"))
        assertEquals(EditorFileType.LOG, EditorFileType.fromFileName("app.log"))
    }

    @Test
    fun unknownOrExtensionlessFilesFallbackToPlainText() {
        assertEquals(EditorFileType.TXT, EditorFileType.fromFileName("notes"))
        assertEquals(EditorFileType.TXT, EditorFileType.fromFileName("archive.bin"))
    }

    @Test
    fun extensionCheckIgnoresLeadingDotAndCase() {
        assertTrue(EditorFileType.isSupportedExtension(".JSON"))
        assertTrue(EditorFileType.isSupportedExtension("YML"))
    }
}
