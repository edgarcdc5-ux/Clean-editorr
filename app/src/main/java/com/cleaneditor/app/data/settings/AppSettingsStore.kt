package com.cleaneditor.app.data.settings

import android.content.Context
import com.cleaneditor.app.theme.AppThemeSetting

/** Persists user preferences so they survive process restarts. */
class AppSettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun loadTheme(): AppThemeSetting = runCatching {
        AppThemeSetting.valueOf(prefs.getString(KEY_THEME, AppThemeSetting.DARK.name) ?: AppThemeSetting.DARK.name)
    }.getOrDefault(AppThemeSetting.DARK)

    fun saveTheme(value: AppThemeSetting) {
        prefs.edit().putString(KEY_THEME, value.name).apply()
    }

    companion object {
        private const val PREFS = "clean_editor_settings"
        private const val KEY_THEME = "theme"
    }
}
