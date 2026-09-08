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
    private val preferences = context.getSharedPreferences("ai_history", Context.MODE_PRIVATE)
    private val key = "entries"
    private val maxEntries = 30

    fun load(): List<AiHistoryEntry> = runCatching {
        val array = JSONArray(preferences.getString(key, "[]"))
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(AiHistoryEntry(item.optLong("timestamp"), item.optString("action"), item.optString("prompt"), item.optString("response")))
            }
        }
    }.getOrDefault(emptyList())

    fun add(entry: AiHistoryEntry) {
        val array = JSONArray()
        (listOf(entry) + load()).take(maxEntries).forEach { item ->
            array.put(JSONObject().put("timestamp", item.timestamp).put("action", item.action).put("prompt", item.prompt).put("response", item.response))
        }
        preferences.edit().putString(key, array.toString()).apply()
    }

    fun clear() = preferences.edit().remove(key).apply()
}
