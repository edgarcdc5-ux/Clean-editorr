package com.cassinopros.app

import android.content.Context

object AppPreferences {
    private const val NAME = "cassino_pros_preferences"
    private const val HIDE_VALUES = "hide_values"
    private const val APP_LOCK = "app_lock"

    fun hideValues(context: Context): Boolean =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getBoolean(HIDE_VALUES, false)

    fun setHideValues(context: Context, enabled: Boolean) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(HIDE_VALUES, enabled).apply()
    }

    fun appLockEnabled(context: Context): Boolean =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getBoolean(APP_LOCK, false)

    fun setAppLockEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(APP_LOCK, enabled).apply()
    }
}
