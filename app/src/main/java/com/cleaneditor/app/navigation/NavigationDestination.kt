package com.cleaneditor.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.cleaneditor.app.R

sealed class NavigationDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    data object Home : NavigationDestination("home", R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home, "nav_item_home")
    data object Files : NavigationDestination("files", R.string.nav_files, Icons.Filled.Folder, Icons.Outlined.Folder, "nav_item_files")
    data object Reminders : NavigationDestination("reminders", R.string.nav_reminders, Icons.Filled.CheckCircle, Icons.Outlined.CheckCircleOutline, "nav_item_reminders")
    data object Ai : NavigationDestination("ai", R.string.nav_ai, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_item_ai")
    data object Settings : NavigationDestination("settings", R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings, "nav_item_settings")
    data object Editor : NavigationDestination("editor", R.string.module_editor_name, Icons.Filled.EditNote, Icons.Outlined.EditNote, "nav_item_editor")

    companion object {
        val items: List<NavigationDestination>
            get() = listOf(Home, Files, Reminders, Ai, Settings)
    }
}
