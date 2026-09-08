package com.cleaneditor.app.data.ai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AiEditorBridge {
    var pendingText by mutableStateOf<String?>(null)
        private set

    fun applyToEditor(text: String) {
        pendingText = text
    }

    fun consume(): String? {
        val text = pendingText
        pendingText = null
        return text
    }
}
