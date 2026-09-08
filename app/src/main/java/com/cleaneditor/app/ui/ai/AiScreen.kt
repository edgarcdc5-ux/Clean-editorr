package com.cleaneditor.app.ui.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cleaneditor.app.R
import com.cleaneditor.app.data.ai.GeminiService
import com.cleaneditor.app.ui.shared.CleanEditorHeader
import kotlinx.coroutines.launch

@Composable
fun AiScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val service = remember { GeminiService() }
    var prompt by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 16.dp).testTag("screen_ai"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CleanEditorHeader(
            title = stringResource(R.string.ai_title),
            subtitle = stringResource(R.string.ai_subtitle)
        )

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it; error = "" },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("ai_prompt_input"),
            minLines = 5,
            label = { Text(stringResource(R.string.ai_prompt_label)) },
            placeholder = { Text(stringResource(R.string.ai_prompt_hint)) }
        )

        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Button(
                enabled = prompt.isNotBlank() && !loading,
                onClick = {
                    loading = true
                    response = ""
                    error = ""
                    scope.launch {
                        service.generate(prompt.trim()).onSuccess { response = it }.onFailure { error = it.message ?: "Erro ao consultar a IA." }
                        loading = false
                    }
                },
                modifier = Modifier.testTag("ai_generate_button")
            ) {
                if (loading) CircularProgressIndicator(strokeWidth = 2.dp) else Text(stringResource(R.string.ai_generate_button))
            }
        }

        if (error.isNotBlank()) {
            Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp).testTag("ai_error"))
        }

        if (response.isNotBlank()) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("ai_response"), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Psychology, contentDescription = null)
                    Text(stringResource(R.string.ai_response_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
                }
                Text(response, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
