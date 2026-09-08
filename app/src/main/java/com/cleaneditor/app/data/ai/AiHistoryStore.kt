package com.cleaneditor.app.data.ai

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class AiHistoryEntry(
    val timestamp: Long,
    val action: String,
    val prompt: String,
    val response: String
)

class AiHistoryStore(context: Context) {
    companion object {
        const val KEY = "entries"
        const val MAX_ENTRIES = 30

        fun trimEntries(existing: List<AiHistoryEntry>, newEntry: AiHistoryEntry): List<AiHistoryEntry> =
            (listOf(newEntry) + existing).take(MAX_ENTRIES)
    }

    private val preferences = context.getSharedPreferences("ai_history", Context.MODE_PRIVATE)

    fun load(): List<AiHistoryEntry> = runCatching {
        val array = JSONArray(preferences.getString(KEY, "[]"))
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    AiHistoryEntry(
                        timestamp = item.optLong("timestamp", System.currentTimeMillis()),
                        action = item.optString("action", "GERAR"),
                        prompt = item.optString("prompt", ""),
                        response = item.optString("response", "")
                    )
                )
            }
        }
    }.getOrDefault(emptyList())

    fun add(entry: AiHistoryEntry) {
        runCatching {
            val safeEntry = entry.copy(
                prompt = entry.prompt.take(4000),
                response = entry.response.take(10000)
            )
            val updated = trimEntries(load(), safeEntry)
            val array = JSONArray()
            updated.forEach { item ->
                array.put(
                    JSONObject()
                        .put("timestamp", item.timestamp)
                        .put("action", item.action)
                        .put("prompt", item.prompt)
                        .put("response", item.response)
                )
            }
            preferences.edit().putString(KEY, array.toString()).apply()
        }
    }

    fun clear() = runCatching { preferences.edit().remove(KEY).apply() }
}
