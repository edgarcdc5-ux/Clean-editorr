package com.cleaneditor.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceGuardTest {
    @Test
    fun editorTextIsCapped() {
        assertEquals(PerformanceGuard.MAX_EDITOR_CHARS, PerformanceGuard.limitEditorText("x".repeat(PerformanceGuard.MAX_EDITOR_CHARS + 500)).length)
    }

    @Test
    fun aiTextIsCapped() {
        assertEquals(PerformanceGuard.MAX_AI_CHARS, PerformanceGuard.limitAiText("x".repeat(PerformanceGuard.MAX_AI_CHARS + 500)).length)
    }

    @Test
    fun chunkingThresholdIsStable() {
        assertFalse(PerformanceGuard.shouldUseChunkedProcessing(20_000))
        assertTrue(PerformanceGuard.shouldUseChunkedProcessing(20_001))
    }
}
