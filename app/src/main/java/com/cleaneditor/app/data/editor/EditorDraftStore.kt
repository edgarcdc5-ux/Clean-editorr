package com.cleaneditor.app.data.editor

import android.content.Context

/** Lightweight local draft persistence for future editor autosave/recovery flows. */
class EditorDraftStore(context: Context) {
    private val prefs = context.getSharedPreferences("cleaneditor_editor_drafts", Context.MODE_PRIVATE)

    fun save(documentId: String, fileName: String, content: String) {
        prefs.edit()
            .putString("${documentId}_name", fileName)
            .putString("${documentId}_content", content.take(MAX_DRAFT_CHARS))
            .putLong("${documentId}_time", System.currentTimeMillis())
            .apply()
    }

    fun load(documentId: String): Draft? {
        val content = prefs.getString("${documentId}_content", null) ?: return null
        return Draft(
            fileName = prefs.getString("${documentId}_name", "Sem título.txt") ?: "Sem título.txt",
            content = content,
            savedAt = prefs.getLong("${documentId}_time", 0L)
        )
    }

    fun clear(documentId: String) {
        prefs.edit()
            .remove("${documentId}_name")
            .remove("${documentId}_content")
            .remove("${documentId}_time")
            .apply()
    }

    data class Draft(val fileName: String, val content: String, val savedAt: Long)

    companion object { const val MAX_DRAFT_CHARS = 100_000 }
}
