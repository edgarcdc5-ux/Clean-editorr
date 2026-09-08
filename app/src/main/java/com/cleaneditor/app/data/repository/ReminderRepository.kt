package com.cleaneditor.app.data.repository

import android.content.Context
import com.cleaneditor.app.data.model.Reminder
import org.json.JSONArray
import org.json.JSONObject

class ReminderRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getAll(): List<Reminder> = synchronized(this) {
        val raw = prefs.getString(KEY_ITEMS, null) ?: return@synchronized emptyList()
        runCatching {
            val array = JSONArray(raw)
            buildList(array.length()) { for (i in 0 until array.length()) add(fromJson(array.getJSONObject(i))) }
        }.getOrDefault(emptyList())
    }

    fun save(reminder: Reminder) = synchronized(this) { persist(getAll().filterNot { it.id == reminder.id } + reminder) }
    fun delete(id: Long) = synchronized(this) { persist(getAll().filterNot { it.id == id }) }
    fun setCompleted(id: Long, completed: Boolean) = synchronized(this) {
        val now = System.currentTimeMillis()
        persist(getAll().map { if (it.id == id) it.copy(completed = completed, updatedAt = now) else it })
    }

    private fun persist(items: List<Reminder>) {
        val array = JSONArray()
        items.forEach { array.put(toJson(it)) }
        prefs.edit().putString(KEY_ITEMS, array.toString()).apply()
    }

    private fun toJson(item: Reminder) = JSONObject().apply {
        put("id", item.id); put("title", item.title); put("content", item.content)
        put("date", item.date); put("priority", item.priority); put("category", item.category)
        put("completed", item.completed); put("createdAt", item.createdAt); put("updatedAt", item.updatedAt)
    }

    private fun fromJson(json: JSONObject) = Reminder(
        id = json.optLong("id"), title = json.optString("title"), content = json.optString("content"),
        date = json.optString("date"), priority = json.optString("priority", "Média"),
        category = json.optString("category", "Geral"), completed = json.optBoolean("completed"),
        createdAt = json.optLong("createdAt", System.currentTimeMillis()),
        updatedAt = json.optLong("updatedAt", System.currentTimeMillis())
    )

    companion object { private const val PREFS = "clean_editor_reminders"; private const val KEY_ITEMS = "items" }
}
