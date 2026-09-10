package com.cleaneditor.app.data.reminders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cleaneditor.app.theme.CleanEditorTheme

class ReminderAlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.getStringExtra(ReminderAlarmService.EXTRA_TITLE).orEmpty().ifBlank { "Lembrete" }
        val content = intent.getStringExtra(ReminderAlarmService.EXTRA_CONTENT).orEmpty()

        setContent {
            CleanEditorTheme {
                AlarmScreen(title, content) {
                    ReminderAlarmService.stop(this@ReminderAlarmActivity)
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        // Back must not dismiss the alarm. The user must explicitly disable it.
    }
}

@Composable
private fun AlarmScreen(title: String, content: String, onStop: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.NotificationsActive,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        Text("LEMBRETE", style = MaterialTheme.typography.labelLarge)
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 8.dp))
        if (content.isNotBlank()) {
            Text(content, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 12.dp))
        }
        Text("Som tocando até desativar", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 20.dp))
        Button(onClick = onStop, modifier = Modifier.padding(top = 28.dp)) {
            Text("DESATIVAR")
        }
    }
}
