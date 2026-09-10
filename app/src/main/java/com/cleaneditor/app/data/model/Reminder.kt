package com.cleaneditor.app.data.model

/** A locally persisted reminder/task. */
data class Reminder(
    val id: Long,
    val title: String,
    val content: String,
    val date: String,
    val priority: String,
    val category: String,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val daily: Boolean = false,
    val alarmTime: String = "",
    val alarmEnabled: Boolean = false
)
