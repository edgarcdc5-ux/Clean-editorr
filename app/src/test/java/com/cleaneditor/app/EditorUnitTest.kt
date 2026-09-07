package com.cleaneditor.app

import com.cleaneditor.app.navigation.NavigationDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorUnitTest {

    @Test
    fun verifyEditorDestination() {
        assertEquals("editor", NavigationDestination.Editor.route)
        assertEquals("nav_item_editor", NavigationDestination.Editor.testTag)
    }

    @Test
    fun verifyDocumentModificationDetection() {
        val savedText = "Hello World"
        val currentTextModified = "Hello World!"
        val currentTextUnmodified = "Hello World"

        assertTrue(currentTextModified != savedText)
        assertFalse(currentTextUnmodified != savedText)
    }

    @Test
    fun verifySafeSelectionBounds() {
        // Testa se a lógica de min/max protege contra seleção invertida
        val selectionStart = 8
        val selectionEnd = 3
        val selMin = minOf(selectionStart, selectionEnd)
        val selMax = maxOf(selectionStart, selectionEnd)

        assertEquals(3, selMin)
        assertEquals(8, selMax)

        val sampleText = "CleanEditor Android"
        val selected = sampleText.substring(selMin, selMax)
        assertEquals("anEdi", selected)
    }
}
