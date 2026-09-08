package com.cleaneditor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.cleaneditor.app.navigation.NavigationDestination
import com.cleaneditor.app.theme.CleanEditorTheme
import com.cleaneditor.app.ui.home.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CleanEditorTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        onNavigateTo = { _: NavigationDestination -> },
                        onOpenEditor = { }
                    )
                }
            }
        }
    }
}

@Composable
private fun CleanEditorHomeDiagnostic() {
    CleanEditorTheme {
        Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            HomeScreen(onNavigateTo = { }, onOpenEditor = { })
        }
    }
}
