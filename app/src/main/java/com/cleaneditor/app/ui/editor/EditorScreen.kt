package com.cleaneditor.app.ui.editor

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

private fun queryFileName(context: Context, uri: Uri): String {
    var name = "Sem título.txt"
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                val displayName = cursor.getString(nameIndex)
                if (!displayName.isNullOrBlank()) {
                    name = displayName
                }
            }
        }
    } catch (_: Exception) {}
    return name
}

@Composable
fun EditorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentUri by remember { mutableStateOf<Uri?>(null) }
    var currentFileName by remember { mutableStateOf("Sem título.txt") }
    var value by remember { mutableStateOf(TextFieldValue("")) }
    var savedText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }
    var searchIndex by remember { mutableStateOf(0) }
    var pendingActionAfterDiscard by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val undoStack = remember { mutableStateListOf<TextFieldValue>() }
    val redoStack = remember { mutableStateListOf<TextFieldValue>() }
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isModified = value.text != savedText

    val occurrences = remember(value.text, searchQuery) {
        if (searchQuery.isBlank()) 0
        else value.text.windowed(searchQuery.length, 1, partialWindows = false)
            .count { it.equals(searchQuery, ignoreCase = true) }
    }

    fun setEditorValue(newValue: TextFieldValue, recordHistory: Boolean = true) {
        if (newValue == value) return
        if (recordHistory) {
            undoStack.add(value)
            if (undoStack.size > 100) undoStack.removeAt(0)
            redoStack.clear()
        }
        value = newValue
    }

    fun resetToNewDocument() {
        currentUri = null
        currentFileName = "Sem título.txt"
        savedText = ""
        setEditorValue(TextFieldValue(""), recordHistory = false)
        undoStack.clear()
        redoStack.clear()
    }

    fun handleGuardedAction(action: () -> Unit) {
        if (isModified) {
            pendingActionAfterDiscard = action
            showDiscardDialog = true
        } else {
            action()
        }
    }

    BackHandler(enabled = isModified) {
        handleGuardedAction(onBack)
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        uri?.let { targetUri ->
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(targetUri, "wt")?.use { outputStream ->
                            outputStream.write(value.text.toByteArray(Charsets.UTF_8))
                            outputStream.flush()
                        } ?: throw IOException("Não foi possível acessar o arquivo de destino.")
                    }
                    val fileName = withContext(Dispatchers.IO) {
                        queryFileName(context, targetUri)
                    }
                    currentUri = targetUri
                    currentFileName = fileName
                    savedText = value.text
                    snackbarHostState.showSnackbar("Arquivo salvo com sucesso: $fileName")
                } catch (e: IOException) {
                    snackbarHostState.showSnackbar("Erro de gravação: ${e.localizedMessage ?: "Falha ao gravar arquivo"}")
                } catch (e: SecurityException) {
                    snackbarHostState.showSnackbar("Permissão negada ao salvar arquivo")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Erro inesperado ao salvar arquivo")
                }
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            scope.launch {
                try {
                    val content = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                                reader.readText()
                            }
                        } ?: throw IOException("Não foi possível acessar o arquivo selecionado.")
                    }
                    val fileName = withContext(Dispatchers.IO) {
                        queryFileName(context, sourceUri)
                    }
                    currentUri = sourceUri
                    currentFileName = fileName
                    savedText = content
                    setEditorValue(TextFieldValue(content), recordHistory = false)
                    undoStack.clear()
                    redoStack.clear()
                    snackbarHostState.showSnackbar("Arquivo aberto: $fileName")
                } catch (e: IOException) {
                    snackbarHostState.showSnackbar("Erro de leitura: ${e.localizedMessage ?: "Falha ao ler arquivo"}")
                } catch (e: SecurityException) {
                    snackbarHostState.showSnackbar("Permissão negada ao abrir arquivo")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Erro inesperado ao abrir arquivo")
                }
            }
        }
    }

    fun saveDirectly() {
        val uri = currentUri
        if (uri == null) {
            createDocumentLauncher.launch(currentFileName)
        } else {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                            outputStream.write(value.text.toByteArray(Charsets.UTF_8))
                            outputStream.flush()
                        } ?: throw IOException("Não foi possível salvar o arquivo.")
                    }
                    savedText = value.text
                    snackbarHostState.showSnackbar("Arquivo salvo: $currentFileName")
                } catch (e: IOException) {
                    snackbarHostState.showSnackbar("Erro de gravação: ${e.localizedMessage ?: "Falha ao gravar arquivo"}")
                } catch (e: SecurityException) {
                    snackbarHostState.showSnackbar("Permissão negada ao salvar arquivo")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Erro inesperado ao salvar arquivo")
                }
            }
        }
    }

    fun moveSearch(direction: Int) {
        if (searchQuery.isBlank()) return
        val starts = buildList {
            var start = 0
            while (true) {
                val found = value.text.indexOf(searchQuery, start, ignoreCase = true)
                if (found < 0) break
                add(found)
                start = found + maxOf(1, searchQuery.length)
            }
        }
        if (starts.isEmpty()) return
        searchIndex = (searchIndex + direction).mod(starts.size)
        val start = starts[searchIndex]
        setEditorValue(
            value.copy(selection = TextRange(start, start + searchQuery.length)),
            recordHistory = false
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = {
                showDiscardDialog = false
                pendingActionAfterDiscard = null
            },
            title = { Text("Descartar alterações?") },
            text = { Text("Existem alterações não salvas no documento atual. Deseja descartá-las?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        val action = pendingActionAfterDiscard
                        pendingActionAfterDiscard = null
                        action?.invoke()
                    },
                    modifier = Modifier.testTag("dialog_btn_discard")
                ) {
                    Text("Descartar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        pendingActionAfterDiscard = null
                    },
                    modifier = Modifier.testTag("dialog_btn_cancel")
                ) {
                    Text("Cancelar")
                }
            },
            modifier = Modifier.testTag("dialog_discard_changes")
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("screen_editor"),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Surface(shadowElevation = 2.dp) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { handleGuardedAction(onBack) },
                            modifier = Modifier.testTag("btn_editor_back")
                        ) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentFileName + if (isModified) " •" else "",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (isModified) "Documento modificado" else if (currentUri != null) "Salvo no dispositivo" else "Documento não salvo",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isModified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { handleGuardedAction { resetToNewDocument() } },
                            modifier = Modifier.testTag("btn_editor_new")
                        ) {
                            Icon(Icons.Filled.Add, "Novo")
                        }
                        IconButton(
                            onClick = {
                                handleGuardedAction {
                                    openDocumentLauncher.launch(
                                        arrayOf("text/*", "application/json", "application/xml", "*/*")
                                    )
                                }
                            },
                            modifier = Modifier.testTag("btn_editor_open")
                        ) {
                            Icon(Icons.Filled.FolderOpen, "Abrir")
                        }
                        IconButton(
                            onClick = { saveDirectly() },
                            modifier = Modifier.testTag("btn_editor_save")
                        ) {
                            Icon(Icons.Filled.Save, "Salvar")
                        }
                        IconButton(
                            onClick = { createDocumentLauncher.launch(currentFileName) },
                            modifier = Modifier.testTag("btn_editor_save_as")
                        ) {
                            Icon(Icons.Filled.SaveAs, "Salvar como")
                        }
                        IconButton(
                            enabled = undoStack.isNotEmpty(),
                            onClick = {
                                if (undoStack.isNotEmpty()) {
                                    redoStack.add(value)
                                    value = undoStack.removeAt(undoStack.lastIndex)
                                }
                            },
                            modifier = Modifier.testTag("btn_editor_undo")
                        ) {
                            Icon(Icons.Filled.Undo, "Desfazer")
                        }
                        IconButton(
                            enabled = redoStack.isNotEmpty(),
                            onClick = {
                                if (redoStack.isNotEmpty()) {
                                    undoStack.add(value)
                                    value = redoStack.removeAt(redoStack.lastIndex)
                                }
                            },
                            modifier = Modifier.testTag("btn_editor_redo")
                        ) {
                            Icon(Icons.Filled.Redo, "Refazer")
                        }
                        IconButton(
                            onClick = {
                                val selMin = value.selection.min
                                val selMax = value.selection.max
                                if (selMin != selMax) {
                                    val selected = value.text.substring(selMin, selMax)
                                    clipboard.setText(AnnotatedString(selected))
                                }
                            },
                            modifier = Modifier.testTag("btn_editor_copy")
                        ) {
                            Icon(Icons.Filled.ContentCopy, "Copiar")
                        }
                        IconButton(
                            onClick = {
                                val selMin = value.selection.min
                                val selMax = value.selection.max
                                if (selMin != selMax) {
                                    val selected = value.text.substring(selMin, selMax)
                                    clipboard.setText(AnnotatedString(selected))
                                    setEditorValue(
                                        value.copy(
                                            text = value.text.removeRange(selMin, selMax),
                                            selection = TextRange(selMin)
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.testTag("btn_editor_cut")
                        ) {
                            Icon(Icons.Filled.ContentCut, "Recortar")
                        }
                        IconButton(
                            onClick = {
                                val pasted = clipboard.getText()?.text ?: return@IconButton
                                val selMin = value.selection.min
                                val selMax = value.selection.max
                                setEditorValue(
                                    value.copy(
                                        text = value.text.replaceRange(selMin, selMax, pasted),
                                        selection = TextRange(selMin + pasted.length)
                                    )
                                )
                            },
                            modifier = Modifier.testTag("btn_editor_paste")
                        ) {
                            Icon(Icons.Filled.ContentPaste, "Colar")
                        }
                        IconButton(
                            onClick = {
                                setEditorValue(value.copy(selection = TextRange(0, value.text.length)), recordHistory = false)
                            },
                            modifier = Modifier.testTag("btn_editor_select_all")
                        ) {
                            Icon(Icons.Filled.SelectAll, "Selecionar tudo")
                        }
                        IconButton(
                            onClick = { searchVisible = !searchVisible },
                            modifier = Modifier.testTag("btn_editor_search")
                        ) {
                            Icon(Icons.Filled.Search, "Pesquisar")
                        }
                    }
                }
            }

            if (searchVisible) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it; searchIndex = 0 },
                        modifier = Modifier.weight(1f).testTag("editor_search_input"),
                        singleLine = true,
                        label = { Text("Pesquisar") }
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("$occurrences", style = MaterialTheme.typography.labelMedium)
                    IconButton(onClick = { moveSearch(-1) }) {
                        Icon(Icons.Filled.KeyboardArrowUp, "Anterior")
                    }
                    IconButton(onClick = { moveSearch(1) }) {
                        Icon(Icons.Filled.KeyboardArrowDown, "Próxima")
                    }
                    IconButton(onClick = { searchVisible = false }) {
                        Icon(Icons.Filled.Close, "Fechar")
                    }
                }
            }

            BasicTextField(
                value = value,
                onValueChange = { setEditorValue(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("editor_text_field"),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (value.text.isEmpty()) {
                        Text("Comece a escrever...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    innerTextField()
                }
            )

            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val lines = if (value.text.isEmpty()) 1 else value.text.count { it == '\n' } + 1
                    val cursorLine = value.text.take(value.selection.start).count { it == '\n' } + 1
                    val lastBreak = value.text.lastIndexOf('\n', value.selection.start - 1)
                    val cursorColumn = value.selection.start - (lastBreak + 1) + 1
                    Text("$lines linhas • ${value.text.length} caracteres", style = MaterialTheme.typography.labelSmall)
                    Text("Ln $cursorLine, Col $cursorColumn", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

