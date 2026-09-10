package com.cleaneditor.app.data.repository

import android.content.Context

/** Keeps a small local list of recently opened file paths. */
class RecentFilesStore(context: Context) {
    private val prefs = context.getSharedPreferences("cleaneditor_recent_files", Context.MODE_PRIVATE)
    private val key = "paths"

    fun add(path: String) {
        val normalized = path.trim()
        if (normalized.isEmpty()) return
        val updated = load().filterNot { it == normalized }.toMutableList().apply { add(0, normalized) }.take(MAX_ENTRIES)
        prefs.edit().putStringSet(key, updated.toSet()).putString(KEY_ORDER, updated.joinToString("\n")).apply()
    }

    fun load(): List<String> = prefs.getString(KEY_ORDER, null)
        ?.lineSequence()?.map(String::trim)?.filter(String::isNotEmpty)?.distinct()?.take(MAX_ENTRIES)?.toList()
        ?: prefs.getStringSet(key, emptySet()).orEmpty().toList().take(MAX_ENTRIES)

    fun clear() { prefs.edit().remove(key).remove(KEY_ORDER).apply() }

    companion object {
        const val MAX_ENTRIES = 20
        private const val KEY_ORDER = "ordered_paths"
    }
}
