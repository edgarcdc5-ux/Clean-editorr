package com.cleaneditor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cleaneditor.app.data.model.FileItem
import com.cleaneditor.app.data.settings.AppSettingsStore
import com.cleaneditor.app.navigation.NavigationDestination
import com.cleaneditor.app.theme.AppThemeSetting
import com.cleaneditor.app.theme.CleanEditorTheme
import com.cleaneditor.app.ui.ai.AiScreen
import com.cleaneditor.app.ui.editor.EditorScreen
import com.cleaneditor.app.ui.files.FilesScreen
import com.cleaneditor.app.ui.home.HomeScreen
import com.cleaneditor.app.ui.reminders.RemindersScreen
import com.cleaneditor.app.ui.settings.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsStore = remember { AppSettingsStore(this@MainActivity) }
            var currentTheme by remember { mutableStateOf(settingsStore.loadTheme()) }
            CleanEditorTheme(themeSetting = currentTheme) {
                CleanEditorShell(currentTheme) { theme ->
                    currentTheme = theme
                    settingsStore.saveTheme(theme)
                }
            }
        }
    }
}

@Composable
private fun CleanEditorShell(currentTheme: AppThemeSetting, onThemeChange: (AppThemeSetting) -> Unit) {
    var activeDestination by remember { mutableStateOf<NavigationDestination>(NavigationDestination.Home) }
    var selectedFile by remember { mutableStateOf<FileItem?>(null) }
    var editorInitialText by remember { mutableStateOf<String?>(null) }
    var aiInitialPrompt by remember { mutableStateOf("") }
    var reminderDraftContent by remember { mutableStateOf<String?>(null) }

    fun openEditor(text: String? = null, file: FileItem? = null) {
        editorInitialText = text
        selectedFile = file
        activeDestination = NavigationDestination.Editor
    }

    fun openAi(prompt: String) {
        aiInitialPrompt = prompt.take(20_000)
        activeDestination = NavigationDestination.Ai
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (activeDestination == NavigationDestination.Editor) {
            EditorScreen(
                onBack = {
                    activeDestination = NavigationDestination.Home
                    selectedFile = null
                    editorInitialText = null
                },
                initialFile = selectedFile,
                initialText = editorInitialText,
                onCreateReminder = { content ->
                    reminderDraftContent = content
                    activeDestination = NavigationDestination.Reminders
                }
            )
        } else {
            Scaffold(Modifier.fillMaxSize().statusBarsPadding()) { innerPadding ->
                Row(Modifier.fillMaxSize().padding(innerPadding)) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .testTag("clean_editor_vertical_menu"),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "CleanEditor",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            NavigationDestination.items.forEach { destination ->
                                val isSelected = activeDestination == destination
                                NavigationRailItem(
                                    modifier = Modifier.testTag(destination.testTag),
                                    selected = isSelected,
                                    onClick = { activeDestination = destination },
                                    icon = {
                                        Icon(
                                            if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                            stringResource(destination.labelRes)
                                        )
                                    },
                                    label = {
                                        Text(
                                            stringResource(destination.labelRes),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                )
                                Spacer(Modifier.height(2.dp))
                            }
                        }
                    }
                    Box(Modifier.fillMaxSize()) {
                        when (activeDestination) {
                            NavigationDestination.Home -> HomeScreen(
                                onNavigateTo = { destination ->
                                    if (destination == NavigationDestination.Editor) openEditor()
                                    else activeDestination = destination
                                },
                                onOpenEditor = { openEditor() }
                            )
                            NavigationDestination.Files -> FilesScreen(
                                onOpenFile = { file -> openEditor(file = file) },
                                onOpenAi = ::openAi
                            )
                            NavigationDestination.Reminders -> RemindersScreen(
                                initialContent = reminderDraftContent,
                                onDraftConsumed = { reminderDraftContent = null },
                                onOpenEditor = { text -> openEditor(text = text) }
                            )
                            NavigationDestination.Ai -> AiScreen(
                                initialPrompt = aiInitialPrompt,
                                onOpenEditor = { response -> openEditor(text = response) }
                            )
                            NavigationDestination.Settings -> SettingsScreen(
                                currentTheme = currentTheme,
                                onThemeChange = onThemeChange
                            )
                            NavigationDestination.Editor -> Unit
                        }
                    }
                }
            }
        }
    }
}
