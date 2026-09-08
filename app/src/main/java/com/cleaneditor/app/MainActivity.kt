package com.cleaneditor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.cleaneditor.app.theme.CleanEditorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CleanEditorTheme {
                Text("CleanEditor\nDiagnóstico do Theme")
            }
        }
    }
}
