package com.cleaneditor.app.util

/** Small, allocation-free guards used before expensive editor/file operations. */
object PerformanceGuard {
    const val MAX_EDITOR_CHARS = 100_000
    const val MAX_AI_CHARS = 20_000

    fun limitEditorText(text: String): String = text.take(MAX_EDITOR_CHARS)
    fun limitAiText(text: String): String = text.take(MAX_AI_CHARS)
    fun shouldUseChunkedProcessing(length: Int): Boolean = length > 20_000
}
