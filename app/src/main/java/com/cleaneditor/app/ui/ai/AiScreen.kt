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
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cleaneditor.app.R
import com.cleaneditor.app.data.ai.AiAction
import com.cleaneditor.app.data.ai.AiHistoryEntry
import com.cleaneditor.app.data.ai.AiHistoryStore
import com.cleaneditor.app.data.ai.GeminiService
import com.cleaneditor.app.data.ai.buildAiPrompt
import com.cleaneditor.app.ui.shared.CleanEditorHeader
import kotlinx.coroutines.launch

@Composable
fun AiScreen(
    onOpenEditor: (String) -> Unit = {},
    initialPrompt: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val service = remember { GeminiService() }
    val historyStore = remember { AiHistoryStore(context) }
    val history = remember { mutableStateListOf<AiHistoryEntry>().also { it.addAll(historyStore.load()) } }
    var prompt by remember { mutableStateOf(initialPrompt.take(GeminiService.MAX_PROMPT_CHARS)) }
    var response by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var selectedAction by remember { mutableStateOf<AiAction?>(null) }

    LaunchedEffect(initialPrompt) {
        if (initialPrompt.isNotBlank()) prompt = initialPrompt.take(GeminiService.MAX_PROMPT_CHARS)
    }

    fun useClipboardText() {
        if (loading) return
        val text = clipboard.getText()?.text.orEmpty()
        if (text.isNotBlank()) {
            prompt = text.take(GeminiService.MAX_PROMPT_CHARS)
            response = ""
            error = ""
        } else {
            error = "A área de transferência está vazia. Selecione e copie um texto no editor primeiro."
        }
    }

    fun saveHistory(action: AiAction?, input: String, output: String) {
        val entry = AiHistoryEntry(System.currentTimeMillis(), action?.name ?: "GERAR", input, output)
        historyStore.add(entry)
        history.add(0, entry)
        while (history.size > 30) history.removeAt(history.lastIndex)
    }

    fun request(promptToSend: String, action: AiAction?) {
        if (loading) return
        loading = true
        response = ""
        error = ""
        selectedAction = action
        scope.launch {
            service.generate(promptToSend)
                .onSuccess { output ->
                    response = output
                    saveHistory(action, promptToSend, output)
                }
                .onFailure { error = it.message ?: "Erro ao consultar a IA." }
            loading = false
        }
    }

    fun runAction(action: AiAction) {
        if (prompt.isBlank()) {
            error = "Informe ou cole um texto antes de usar uma ação de IA."
            return
        }
        request(buildAiPrompt(action, prompt.trim()), action)
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 16.dp).testTag("screen_ai"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CleanEditorHeader(stringResource(R.string.ai_title), stringResource(R.string.ai_subtitle))
        OutlinedButton(
            enabled = !loading,
            onClick = { useClipboardText() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("ai_use_clipboard")
        ) {
            Icon(Icons.Filled.ContentPaste, null)
            Text(stringResource(R.string.ai_use_clipboard), Modifier.padding(start = 8.dp))
        }
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it.take(GeminiService.MAX_PROMPT_CHARS); error = "" },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("ai_prompt_input"),
            minLines = 5,
            label = { Text(stringResource(R.string.ai_prompt_label)) },
            placeholder = { Text(stringResource(R.string.ai_prompt_hint)) },
            supportingText = { Text("${prompt.length}/${GeminiService.MAX_PROMPT_CHARS}") }
        )
        Text(stringResource(R.string.ai_actions_title), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 16.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton(AiAction.SUMMARIZE, selectedAction, loading, { runAction(AiAction.SUMMARIZE) }, "Resumir", "ai_action_summarize")
            ActionButton(AiAction.CORRECT, selectedAction, loading, { runAction(AiAction.CORRECT) }, "Corrigir", "ai_action_correct")
            ActionButton(AiAction.REWRITE, selectedAction, loading, { runAction(AiAction.REWRITE) }, "Reescrever", "ai_action_rewrite")
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton(AiAction.EXPAND, selectedAction, loading, { runAction(AiAction.EXPAND) }, "Expandir", "ai_action_expand")
            ActionButton(AiAction.FORMALIZE, selectedAction, loading, { runAction(AiAction.FORMALIZE) }, "Formalizar", "ai_action_formalize")
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton(AiAction.EXPLAIN, selectedAction, loading, { runAction(AiAction.EXPLAIN) }, "Explicar", "ai_action_explain")
            ActionButton(AiAction.TASK, selectedAction, loading, { runAction(AiAction.TASK) }, "Virar tarefa", "ai_action_task")
        }
        Button(
            enabled = prompt.isNotBlank() && !loading,
            onClick = { request(prompt.trim(), null) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("ai_generate_button")
        ) {
            if (loading) CircularProgressIndicator(strokeWidth = 2.dp) else Text(stringResource(R.string.ai_generate_button))
        }
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp).testTag("ai_error"))
        if (response.isNotBlank()) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("ai_response"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Psychology, null)
                    Text(stringResource(R.string.ai_response_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
                }
                Text(response, style = MaterialTheme.typography.bodyLarge)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { clipboard.setText(androidx.compose.ui.text.AnnotatedString(response)) }, modifier = Modifier.weight(1f).testTag("ai_copy_response")) { Text("Copiar") }
                    Button(onClick = { onOpenEditor(response) }, modifier = Modifier.weight(1f).testTag("ai_apply_editor")) { Text("Aplicar no editor") }
                }
            }
        }
        if (history.isNotEmpty()) {
            Text("Histórico recente", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 16.dp))
            history.take(10).forEachIndexed { index, entry ->
                OutlinedButton(
                    onClick = { prompt = entry.prompt; response = entry.response; selectedAction = AiAction.entries.firstOrNull { it.name == entry.action }; error = "" },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("ai_history_$index")
                ) { Text("${entry.action}: ${entry.response.replace("\n", " ").take(90)}") }
            }
            Text("Até 30 resultados são mantidos apenas neste dispositivo.", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun ActionButton(action: AiAction, selectedAction: AiAction?, loading: Boolean, onClick: () -> Unit, label: String, tag: String) {
    OutlinedButton(enabled = !loading, onClick = onClick, modifier = Modifier.testTag(tag)) {
        Text(if (selectedAction == action && loading) "…" else label)
    }
}
