package com.cleaneditor.app.ui.reminders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cleaneditor.app.data.model.Reminder
import com.cleaneditor.app.data.reminders.ReminderAlarmScheduler
import com.cleaneditor.app.data.repository.ReminderRepository
import com.cleaneditor.app.ui.shared.CleanEditorHeader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val priorities = listOf("Baixa", "Média", "Alta")
private val statusFilters = listOf("Todos", "Pendentes", "Concluídos")
private val storageDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

@Composable
fun RemindersScreen(
    modifier: Modifier = Modifier,
    initialContent: String? = null,
    onDraftConsumed: () -> Unit = {},
    onOpenEditor: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember(context) { ReminderRepository(context) }
    val scheduler = remember(context) { ReminderAlarmScheduler(context) }
    var reminders by remember { mutableStateOf(repository.getAll()) }
    var editing by remember { mutableStateOf<Reminder?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Reminder?>(null) }
    var formInitialContent by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("Todos") }
    var pendingAlarmReminder by remember { mutableStateOf<Reminder?>(null) }
    val requestNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        pendingAlarmReminder?.let { if (granted) scheduler.schedule(it) }
        pendingAlarmReminder = null
    }

    LaunchedEffect(initialContent) {
        if (initialContent != null) {
            formInitialContent = initialContent
            creating = true
            onDraftConsumed()
        }
    }

    fun refresh() { reminders = repository.getAll() }

    fun saveReminder(item: Reminder) {
        repository.save(item)
        if (item.daily && item.alarmEnabled && !item.completed) {
            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                pendingAlarmReminder = item
                requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else scheduler.schedule(item)
        } else scheduler.cancel(item.id)
        refresh()
        creating = false
        editing = null
        formInitialContent = ""
    }

    if (creating || editing != null) {
        ReminderEditorForm(
            editing,
            formInitialContent,
            { creating = false; editing = null; formInitialContent = "" },
            ::saveReminder
        )
        return
    }

    val filtered = reminders
        .filter { item ->
            val query = searchQuery.trim()
            query.isBlank() || listOf(item.title, item.content, item.category, item.priority, item.date, item.alarmTime)
                .any { it.contains(query, ignoreCase = true) }
        }
        .filter { item ->
            when (statusFilter) {
                "Pendentes" -> !item.completed
                "Concluídos" -> item.completed
                else -> true
            }
        }
        .sortedWith(compareBy<Reminder> { it.completed }.thenBy { it.date.ifBlank { "9999-99-99" } }.thenByDescending { priorities.indexOf(it.priority) })

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("screen_reminders"),
        floatingActionButton = {
            FloatingActionButton(onClick = { formInitialContent = ""; creating = true }, modifier = Modifier.testTag("btn_reminder_add")) {
                Icon(Icons.Filled.Add, "Novo lembrete")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            CleanEditorHeader(title = "Lembretes & Tarefas", subtitle = "Persistentes, organizados por data, prioridade e categoria")
            OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).testTag("reminder_search"), label = { Text("Pesquisar lembretes") }, singleLine = true)
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                statusFilters.forEach { filter -> TextButton(onClick = { statusFilter = filter }, modifier = Modifier.testTag("reminder_filter_${filter.lowercase()}")) { Text(if (statusFilter == filter) "✓ $filter" else filter) } }
            }
            if (reminders.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.NotificationsActive, null, modifier = Modifier.padding(8.dp))
                    Text("Nenhum lembrete criado", style = MaterialTheme.typography.titleMedium)
                    Text("Crie um lembrete para começar.", modifier = Modifier.padding(top = 6.dp))
                }
            } else if (filtered.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Nenhum resultado", style = MaterialTheme.typography.titleMedium)
                    Text("Tente outra pesquisa ou filtro.", modifier = Modifier.padding(top = 6.dp))
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered, key = { it.id }) { item ->
                        ReminderCard(item, { val next = !item.completed; repository.setCompleted(item.id, next); if (next) scheduler.cancel(item.id) else if (item.daily && item.alarmEnabled) scheduler.schedule(item); refresh() }, { editing = item }, { deleteTarget = item }, { onOpenEditor(item.content) })
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }

    deleteTarget?.let { item ->
        AlertDialog(onDismissRequest = { deleteTarget = null }, title = { Text("Excluir lembrete?") }, text = { Text("\"${item.title}\" será removido permanentemente deste dispositivo.") },
            confirmButton = { TextButton(onClick = { scheduler.cancel(item.id); repository.delete(item.id); deleteTarget = null; refresh() }) { Text("Excluir") } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancelar") } })
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
                    if (item.daily && item.alarmEnabled && item.alarmTime.isNotBlank()) Text("🔔 Diário às ${item.alarmTime}", style = MaterialTheme.typography.labelMedium)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderEditorForm(initial: Reminder?, initialContent: String, onCancel: () -> Unit, onSave: (Reminder) -> Unit) {
    var title by remember(initial?.id, initialContent) { mutableStateOf(initial?.title.orEmpty()) }
    var content by remember(initial?.id, initialContent) { mutableStateOf(initial?.content ?: initialContent) }
    var date by remember(initial?.id) { mutableStateOf(initial?.date.orEmpty()) }
    var priority by remember(initial?.id) { mutableStateOf(initial?.priority ?: "Média") }
    var category by remember(initial?.id) { mutableStateOf(initial?.category ?: "Geral") }
    var daily by remember(initial?.id) { mutableStateOf(initial?.daily ?: false) }
    var alarmTime by remember(initial?.id) { mutableStateOf(initial?.alarmTime ?: "08:00") }
    var alarmEnabled by remember(initial?.id) { mutableStateOf(initial?.alarmEnabled ?: true) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val valid = title.isNotBlank()
    val validTime = alarmTime.matches(Regex("^([01]\\d|2[0-3]):[0-5]\\d$"))
    val scrollState = rememberScrollState()

    Column(Modifier.fillMaxSize().testTag("screen_reminder_form")) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            CleanEditorHeader(title = if (initial == null) "Novo lembrete" else "Editar lembrete", subtitle = "Todos os campos ficam salvos localmente")
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth().testTag("reminder_title"), label = { Text("Título") }, singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(content, { content = it }, Modifier.fillMaxWidth().height(150.dp).testTag("reminder_content"), label = { Text("Conteúdo") })
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = date.toDisplayDate(),
                onValueChange = {},
                modifier = Modifier.fillMaxWidth().testTag("reminder_date"),
                label = { Text("Data") },
                readOnly = true,
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Selecionar data") },
                trailingIcon = { TextButton(onClick = { showDatePicker = true }) { Text("Calendário") } }
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(category, { category = it }, Modifier.fillMaxWidth().testTag("reminder_category"), label = { Text("Categoria") }, singleLine = true)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = daily, onCheckedChange = { daily = it }, modifier = Modifier.testTag("reminder_daily"))
                Text("Repetir todos os dias")
            }
            if (daily) {
                OutlinedTextField(
                    value = alarmTime,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth().testTag("reminder_alarm_time"),
                    label = { Text("Horário do alarme") },
                    readOnly = true,
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Schedule, contentDescription = "Selecionar horário") },
                    trailingIcon = { TextButton(onClick = { showTimePicker = true }) { Text("Escolher") } }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it }, modifier = Modifier.testTag("reminder_alarm_enabled"))
                    Text("Ativar alarme diário")
                }
                if (!validTime) Text("Horário inválido.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            Text("Prioridade", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { priorities.forEach { value -> TextButton(onClick = { priority = value }, modifier = Modifier.testTag("priority_$value")) { Text(if (priority == value) "✓ $value" else value) } } }
            Spacer(Modifier.height(16.dp))
        }

        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) { Text("Cancelar") }
            Button(enabled = valid && (!daily || validTime), onClick = {
                val now = System.currentTimeMillis()
                onSave(initial?.copy(title = title.trim(), content = content, date = date, priority = priority, category = category.trim(), updatedAt = now, daily = daily, alarmTime = if (daily) alarmTime else "", alarmEnabled = daily && alarmEnabled)
                    ?: Reminder(now, title.trim(), content, date, priority, category.trim(), false, now, now, daily, if (daily) alarmTime else "", daily && alarmEnabled))
            }, modifier = Modifier.testTag("btn_reminder_save")) { Icon(Icons.Filled.Check, null); Text("Salvar") }
        }
    }

    if (showDatePicker) {
        val initialMillis = date.toPickerMillis()
        val dateState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { date = it.toStorageDate() }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = dateState) }
    }

    if (showTimePicker) {
        val parsed = alarmTime.split(":").mapNotNull { it.toIntOrNull() }
        val timeState = rememberTimePickerState(initialHour = parsed.getOrNull(0) ?: 8, initialMinute = parsed.getOrNull(1) ?: 0, is24Hour = true)
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    alarmTime = "%02d:%02d".format(Locale.US, timeState.hour, timeState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") } }
        ) { TimePicker(state = timeState) }
    }
}

private fun String.toDisplayDate(): String = runCatching {
    storageDateFormat.parse(this)?.let(displayDateFormat::format)
}.getOrNull() ?: this

private fun String.toPickerMillis(): Long? = runCatching { storageDateFormat.parse(this)?.time }.getOrNull()

private fun Long.toStorageDate(): String = runCatching { storageDateFormat.format(Date(this)) }.getOrDefault("")

private fun Long.toStorageDate(): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = this@toStorageDate }
    return storageDateFormat.format(calendar.time)
}
