package com.cleaneditor.app

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "CleanEditor\nDiagnóstico de inicialização"
            textSize = 22f
            setPadding(32, 32, 32, 32)
        })
    }
}
