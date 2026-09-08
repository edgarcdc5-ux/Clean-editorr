package com.cleaneditor.app.ui.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.cleaneditor.app.data.model.Reminder
import com.cleaneditor.app.data.repository.ReminderRepository
import com.cleaneditor.app.ui.shared.CleanEditorHeader

private val priorities = listOf("Baixa", "Média", "Alta")

@Composable
fun RemindersScreen(
    modifier: Modifier = Modifier,
    initialContent: String? = null,
    onDraftConsumed: () -> Unit = {},
    onOpenEditor: (String) -> Unit = {}
) {
    val repository = remember { ReminderRepository(LocalContext.current) }
    var reminders by remember { mutableStateOf(repository.getAll()) }
    var editing by remember { mutableStateOf<Reminder?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Reminder?>(null) }
    var formInitialContent by remember { mutableStateOf("") }

    LaunchedEffect(initialContent) {
        if (initialContent != null) {
            formInitialContent = initialContent
            creating = true
            onDraftConsumed()
        }
    }
    fun refresh() { reminders = repository.getAll() }

    if (creating || editing != null) {
        ReminderEditorForm(editing, formInitialContent, { creating = false; editing = null; formInitialContent = "" }, { item -> repository.save(item); refresh(); creating = false; editing = null; formInitialContent = "" })
        return
    }

    Scaffold(modifier = modifier.fillMaxSize().testTag("screen_reminders"), floatingActionButton = {
        FloatingActionButton(onClick = { formInitialContent = ""; creating = true }, modifier = Modifier.testTag("btn_reminder_add")) { Icon(Icons.Filled.Add, "Novo lembrete") }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            CleanEditorHeader(title = "Lembretes & Tarefas", subtitle = "Persistentes, organizados por data, prioridade e categoria")
            if (reminders.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.NotificationsActive, null, modifier = Modifier.padding(8.dp))
                    Text("Nenhum lembrete criado", style = MaterialTheme.typography.titleMedium)
                    Text("Crie um lembrete para começar.", modifier = Modifier.padding(top = 6.dp))
                }
            } else {
                val sorted = reminders.sortedWith(compareBy<Reminder> { it.completed }.thenBy { it.date.ifBlank { "9999-99-99" } }.thenByDescending { priorities.indexOf(it.priority) })
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sorted, key = { it.id }) { item ->
                        ReminderCard(item, { repository.setCompleted(item.id, !item.completed); refresh() }, { editing = item }, { deleteTarget = item }, { onOpenEditor(item.content) })
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }
    deleteTarget?.let { item ->
        AlertDialog(onDismissRequest = { deleteTarget = null }, title = { Text("Excluir lembrete?") }, text = { Text("\"${item.title}\" será removido permanentemente deste dispositivo.") }, confirmButton = { TextButton(onClick = { repository.delete(item.id); deleteTarget = null; refresh() }) { Text("Excluir") } }, dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancelar") } })
    }
}

@Composable
private fun ReminderCard(item: Reminder, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit, onOpenEditor: () -> Unit) {
    Card(Modifier.fillMaxWidth().testTag("reminder_${item.id}")) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = item.completed, onCheckedChange = { onToggle() }, modifier = Modifier.testTag("reminder_check_${item.id}"))
                Column(Modifier.weight(1f)) {
                    Text(item.title.ifBlank { "Sem título" }, style = MaterialTheme.typography.titleMedium, textDecoration = if (item.completed) TextDecoration.LineThrough else null)
                    Text("${item.date.ifBlank { "Sem data" }} • ${item.priority} • ${item.category.ifBlank { "Geral" }}", style = MaterialTheme.typography.labelMedium)
                }
                IconButton(onClick = onEdit, modifier = Modifier.testTag("reminder_edit_${item.id}")) { Icon(Icons.Filled.Edit, "Editar") }
                IconButton(onClick = onDelete, modifier = Modifier.testTag("reminder_delete_${item.id}")) { Icon(Icons.Filled.Delete, "Excluir") }
            }
            if (item.content.isNotBlank()) {
                Text(item.content, Modifier.padding(start = 12.dp, top = 4.dp, end = 12.dp), maxLines = 4)
                TextButton(onClick = onOpenEditor, modifier = Modifier.testTag("reminder_open_editor_${item.id}")) { Icon(Icons.Filled.OpenInNew, null); Text("Abrir conteúdo no editor") }
            }
        }
    }
}

@Composable
private fun ReminderEditorForm(initial: Reminder?, initialContent: String, onCancel: () -> Unit, onSave: (Reminder) -> Unit) {
    var title by remember(initial?.id, initialContent) { mutableStateOf(initial?.title.orEmpty()) }
    var content by remember(initial?.id, initialContent) { mutableStateOf(initial?.content ?: initialContent) }
    var date by remember(initial?.id) { mutableStateOf(initial?.date.orEmpty()) }
    var priority by remember(initial?.id) { mutableStateOf(initial?.priority ?: "Média") }
    var category by remember(initial?.id) { mutableStateOf(initial?.category ?: "Geral") }
    val valid = title.isNotBlank()
    Column(Modifier.fillMaxSize().padding(16.dp).testTag("screen_reminder_form")) {
        CleanEditorHeader(title = if (initial == null) "Novo lembrete" else "Editar lembrete", subtitle = "Todos os campos ficam salvos localmente")
        OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth().testTag("reminder_title"), label = { Text("Título") }, singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(content, { content = it }, Modifier.fillMaxWidth().height(150.dp).testTag("reminder_content"), label = { Text("Conteúdo") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(date, { date = it }, Modifier.fillMaxWidth().testTag("reminder_date"), label = { Text("Data (AAAA-MM-DD)") }, singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(category, { category = it }, Modifier.fillMaxWidth().testTag("reminder_category"), label = { Text("Categoria") }, singleLine = true)
        Spacer(Modifier.height(8.dp))
        Text("Prioridade", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { priorities.forEach { value -> TextButton(onClick = { priority = value }, modifier = Modifier.testTag("priority_$value")) { Text(if (priority == value) "✓ $value" else value) } } }
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) { Text("Cancelar") }
            Button(enabled = valid, onClick = {
                val now = System.currentTimeMillis()
                onSave(initial?.copy(title = title.trim(), content = content, date = date.trim(), priority = priority, category = category.trim(), updatedAt = now) ?: Reminder(now, title.trim(), content, date.trim(), priority, category.trim(), false, now, now))
            }, modifier = Modifier.testTag("btn_reminder_save")) { Icon(Icons.Filled.Check, null); Text("Salvar") }
        }
    }
}
