package com.cleaneditor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cleaneditor.app.data.model.FileItem
import com.cleaneditor.app.navigation.NavigationDestination
import com.cleaneditor.app.theme.AppThemeSetting
import com.cleaneditor.app.theme.CleanEditorTheme
import com.cleaneditor.app.ui.ai.AiScreen
import com.cleaneditor.app.ui.editor.EditorScreen
import com.cleaneditor.app.ui.files.FilesScreen
import com.cleaneditor.app.ui.home.HomeScreen
import com.cleaneditor.app.ui.reminders.RemindersScreen
import com.cleaneditor.app.ui.settings.SettingsScreen
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var themeSetting by remember { mutableStateOf(AppThemeSetting.DARK) }
            CleanEditorTheme(themeSetting = themeSetting) { CleanEditorApp(themeSetting, { themeSetting = it }) }
        }
    }
}

@Composable
fun CleanEditorApp(currentTheme: AppThemeSetting, onThemeChange: (AppThemeSetting) -> Unit, modifier: Modifier = Modifier) {
    var activeDestination by remember { mutableStateOf<NavigationDestination>(NavigationDestination.Home) }
    var editorOpen by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<FileItem?>(null) }
    val context = LocalContext.current

    fun openAiResultInEditor(text: String) {
        val file = File.createTempFile("cleaneditor_ai_", ".txt", context.cacheDir).apply { writeText(text, Charsets.UTF_8) }
        selectedFile = FileItem("ai-${file.name}", file.name, file.absolutePath, false, file.length(), file.lastModified(), "txt", false)
        editorOpen = true
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (editorOpen) {
            EditorScreen(onBack = { editorOpen = false; selectedFile = null }, initialFile = selectedFile)
        } else {
            Scaffold(modifier = Modifier.fillMaxSize().statusBarsPadding(), bottomBar = { CleanEditorBottomBar(activeDestination) { activeDestination = it } }) { innerPadding ->
                Box(Modifier.fillMaxSize().padding(innerPadding)) {
                    when (activeDestination) {
                        NavigationDestination.Home -> HomeScreen(onNavigateTo = { destination -> if (destination == NavigationDestination.Editor) { selectedFile = null; editorOpen = true } else activeDestination = destination }, onOpenEditor = { selectedFile = null; editorOpen = true })
                        NavigationDestination.Files -> FilesScreen(onOpenFile = { file -> selectedFile = file; editorOpen = true })
                        NavigationDestination.Reminders -> RemindersScreen()
                        NavigationDestination.Ai -> AiScreen(onOpenEditor = ::openAiResultInEditor)
                        NavigationDestination.Settings -> SettingsScreen(currentTheme = currentTheme, onThemeChange = onThemeChange)
                        NavigationDestination.Editor -> { selectedFile = null; editorOpen = true }
                    }
                }
            }
        }
    }
}

@Composable
private fun CleanEditorBottomBar(currentDestination: NavigationDestination, onDestinationSelected: (NavigationDestination) -> Unit) {
    NavigationBar(modifier = Modifier.navigationBarsPadding().testTag("clean_editor_bottom_bar"), containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
        NavigationDestination.items.forEach { destination ->
            val isSelected = currentDestination == destination
            NavigationBarItem(modifier = Modifier.testTag(destination.testTag), selected = isSelected, onClick = { onDestinationSelected(destination) }, icon = { Icon(if (isSelected) destination.selectedIcon else destination.unselectedIcon, stringResource(destination.labelRes)) }, label = { Text(stringResource(destination.labelRes), style = MaterialTheme.typography.labelSmall) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant, unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}
