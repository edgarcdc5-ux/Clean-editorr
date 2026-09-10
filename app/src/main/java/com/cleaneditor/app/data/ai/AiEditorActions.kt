package com.cleaneditor.app.data.ai

/** Prompt helpers used when AI is invoked from the editor. */
object AiEditorActions {
    const val MAX_SELECTION_CHARS = 20_000

    fun promptForSelection(action: AiAction, selection: String): String {
        val text = selection.trim().take(MAX_SELECTION_CHARS)
        require(text.isNotBlank()) { "Selecione um texto antes de usar a IA." }
        return buildAiPrompt(action, text)
    }

    fun promptForSelection(action: AiAction, selectionStart: Int, selectionEnd: Int, document: String): String {
        val start = minOf(selectionStart, selectionEnd).coerceIn(0, document.length)
        val end = maxOf(selectionStart, selectionEnd).coerceIn(start, document.length)
        return promptForSelection(action, document.substring(start, end))
    }
}
