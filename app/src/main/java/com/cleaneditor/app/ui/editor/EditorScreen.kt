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
import androidx.compose.runtime.LaunchedEffect
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
import com.cleaneditor.app.data.model.EditorFileType
import com.cleaneditor.app.data.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

private fun queryFileName(context: Context, uri: Uri): String {
    var name = "Sem título.txt"
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index)?.takeIf { it.isNotBlank() }?.let { name = it }
        }
    } catch (_: Exception) { }
    return name
}

@Composable
fun EditorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialFile: FileItem? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var currentUri by remember { mutableStateOf<Uri?>(null) }
    var currentInternalFile by remember { mutableStateOf<File?>(null) }
    var currentFileName by remember { mutableStateOf("Sem título.txt") }
    var value by remember { mutableStateOf(TextFieldValue("")) }
    var savedText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }
    var searchIndex by remember { mutableStateOf(0) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val undoStack = remember { mutableStateListOf<TextFieldValue>() }
    val redoStack = remember { mutableStateListOf<TextFieldValue>() }
    val isModified = value.text != savedText
    val editorFileType = remember(currentFileName) { EditorFileType.fromFileName(currentFileName) }

    fun setEditorValue(newValue: TextFieldValue, history: Boolean = true) {
        if (newValue == value) return
        if (history) {
            undoStack.add(value)
            if (undoStack.size > 100) undoStack.removeAt(0)
            redoStack.clear()
        }
        value = newValue
    }

    fun guarded(action: () -> Unit) {
        if (isModified) { pendingAction = action; showDiscardDialog = true } else action()
    }

    fun resetNew() {
        currentUri = null
        currentInternalFile = null
        currentFileName = "Sem título.txt"
        savedText = ""
        setEditorValue(TextFieldValue(""), false)
        undoStack.clear(); redoStack.clear()
    }

    BackHandler { guarded(onBack) }

    LaunchedEffect(initialFile?.path) {
        val file = initialFile?.file ?: return@LaunchedEffect
        try {
            val content = withContext(Dispatchers.IO) { file.readText(Charsets.UTF_8) }
            currentInternalFile = file
            currentUri = null
            currentFileName = file.name
            savedText = content
            setEditorValue(TextFieldValue(content), false)
            undoStack.clear(); redoStack.clear()
        } catch (e: Exception) {
            snackbarHostState.showSnackbar("Erro ao abrir arquivo: ${e.localizedMessage ?: "falha de leitura"}")
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(value.text.toByteArray(Charsets.UTF_8)) }
                        ?: throw IOException("Não foi possível acessar o destino.")
                }
                currentUri = uri
                currentInternalFile = null
                currentFileName = withContext(Dispatchers.IO) { queryFileName(context, uri) }
                savedText = value.text
                snackbarHostState.showSnackbar("Arquivo salvo: $currentFileName")
            } catch (e: SecurityException) { snackbarHostState.showSnackbar("Permissão negada ao salvar arquivo")
            } catch (e: IOException) { snackbarHostState.showSnackbar("Erro de gravação: ${e.localizedMessage ?: "falha"}")
            } catch (_: Exception) { snackbarHostState.showSnackbar("Erro inesperado ao salvar arquivo") }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { input -> BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { it.readText() } }
                        ?: throw IOException("Não foi possível acessar o arquivo.")
                }
                currentUri = uri
                currentInternalFile = null
                currentFileName = withContext(Dispatchers.IO) { queryFileName(context, uri) }
                savedText = content
                setEditorValue(TextFieldValue(content), false)
                undoStack.clear(); redoStack.clear()
                snackbarHostState.showSnackbar("Arquivo aberto: $currentFileName")
            } catch (e: SecurityException) { snackbarHostState.showSnackbar("Permissão negada ao abrir arquivo")
            } catch (e: IOException) { snackbarHostState.showSnackbar("Erro de leitura: ${e.localizedMessage ?: "falha"}")
            } catch (_: Exception) { snackbarHostState.showSnackbar("Erro inesperado ao abrir arquivo") }
        }
    }

    fun saveDirectly() {
        val internal = currentInternalFile
        val uri = currentUri
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    when {
                        internal != null -> internal.writeText(value.text, Charsets.UTF_8)
                        uri != null -> context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(value.text.toByteArray(Charsets.UTF_8)) }
                            ?: throw IOException("Não foi possível salvar o arquivo.")
                        else -> throw IllegalStateException("NO_DESTINATION")
                    }
                }
                savedText = value.text
                snackbarHostState.showSnackbar("Arquivo salvo: $currentFileName")
            } catch (e: IllegalStateException) { if (e.message == "NO_DESTINATION") createDocumentLauncher.launch(currentFileName) else snackbarHostState.showSnackbar("Erro ao salvar arquivo")
            } catch (e: SecurityException) { snackbarHostState.showSnackbar("Permissão negada ao salvar arquivo")
            } catch (e: IOException) { snackbarHostState.showSnackbar("Erro de gravação: ${e.localizedMessage ?: "falha"}")
            } catch (_: Exception) { snackbarHostState.showSnackbar("Erro inesperado ao salvar arquivo") }
        }
    }

    fun moveSearch(direction: Int) {
        if (searchQuery.isBlank()) return
        val starts = buildList {
            var start = 0
            while (start < value.text.length) {
                val found = value.text.indexOf(searchQuery, start, ignoreCase = true)
                if (found < 0) break
                add(found); start = found + maxOf(1, searchQuery.length)
            }
        }
        if (starts.isEmpty()) return
        searchIndex = (searchIndex + direction).mod(starts.size)
        val start = starts[searchIndex]
        setEditorValue(value.copy(selection = TextRange(start, start + searchQuery.length)), false)
    }

    val occurrences = remember(value.text, searchQuery) {
        if (searchQuery.isBlank()) 0 else value.text.windowed(searchQuery.length, 1, false).count { it.equals(searchQuery, true) }
    }
    val line = value.text.take(value.selection.start).count { it == '\n' } + 1
    val lastBreak = value.text.lastIndexOf('\n', value.selection.start.coerceAtLeast(0) - 1)
    val column = value.selection.start - lastBreak
    val lines = if (value.text.isEmpty()) 1 else value.text.count { it == '\n' } + 1

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false; pendingAction = null },
            title = { Text("Descartar alterações?") },
            text = { Text("Existem alterações não salvas em $currentFileName.") },
            confirmButton = { TextButton(onClick = { showDiscardDialog = false; val action = pendingAction; pendingAction = null; action?.invoke() }, modifier = Modifier.testTag("dialog_btn_discard")) { Text("Descartar") } },
            dismissButton = { TextButton(onClick = { showDiscardDialog = false; pendingAction = null }, modifier = Modifier.testTag("dialog_btn_cancel")) { Text("Cancelar") } },
            modifier = Modifier.testTag("dialog_discard_changes")
        )
    }

    Scaffold(modifier = modifier.fillMaxSize().testTag("screen_editor"), snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Surface(shadowElevation = 2.dp) {
                Column {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { guarded(onBack) }, modifier = Modifier.testTag("btn_editor_back")) { Icon(Icons.Filled.ArrowBack, "Voltar") }
                        Column(Modifier.weight(1f)) {
                            Text(currentFileName + if (isModified) " •" else "", style = MaterialTheme.typography.titleMedium)
                            Text(if (isModified) "Documento modificado" else if (currentUri != null || currentInternalFile != null) "Salvo no dispositivo" else "Documento não salvo", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(onClick = { guarded { resetNew() } }, modifier = Modifier.testTag("btn_editor_new")) { Icon(Icons.Filled.Add, "Novo") }
                        IconButton(onClick = { guarded { openDocumentLauncher.launch(arrayOf("text/*", "application/json", "application/xml", "*/*")) } }, modifier = Modifier.testTag("btn_editor_open")) { Icon(Icons.Filled.FolderOpen, "Abrir") }
                        IconButton(onClick = { saveDirectly() }, modifier = Modifier.testTag("btn_editor_save")) { Icon(Icons.Filled.Save, "Salvar") }
                        IconButton(onClick = { createDocumentLauncher.launch(currentFileName) }, modifier = Modifier.testTag("btn_editor_save_as")) { Icon(Icons.Filled.SaveAs, "Salvar como") }
                        IconButton(enabled = undoStack.isNotEmpty(), onClick = { if (undoStack.isNotEmpty()) { redoStack.add(value); value = undoStack.removeAt(undoStack.lastIndex) } }, modifier = Modifier.testTag("btn_editor_undo")) { Icon(Icons.Filled.Undo, "Desfazer") }
                        IconButton(enabled = redoStack.isNotEmpty(), onClick = { if (redoStack.isNotEmpty()) { undoStack.add(value); value = redoStack.removeAt(redoStack.lastIndex) } }, modifier = Modifier.testTag("btn_editor_redo")) { Icon(Icons.Filled.Redo, "Refazer") }
                        IconButton(onClick = { val a=value.selection.min; val b=value.selection.max; if (a != b) clipboard.setText(AnnotatedString(value.text.substring(a,b))) }, modifier = Modifier.testTag("btn_editor_copy")) { Icon(Icons.Filled.ContentCopy, "Copiar") }
                        IconButton(onClick = { val a=value.selection.min; val b=value.selection.max; if (a != b) { clipboard.setText(AnnotatedString(value.text.substring(a,b))); setEditorValue(value.copy(text=value.text.removeRange(a,b), selection=TextRange(a))) } }, modifier = Modifier.testTag("btn_editor_cut")) { Icon(Icons.Filled.ContentCut, "Recortar") }
                        IconButton(onClick = { val pasted=clipboard.getText()?.text ?: return@IconButton; val a=value.selection.min; val b=value.selection.max; setEditorValue(value.copy(text=value.text.replaceRange(a,b,pasted), selection=TextRange(a+pasted.length))) }, modifier = Modifier.testTag("btn_editor_paste")) { Icon(Icons.Filled.ContentPaste, "Colar") }
                        IconButton(onClick = { setEditorValue(value.copy(selection=TextRange(0,value.text.length)), false) }, modifier = Modifier.testTag("btn_editor_select_all")) { Icon(Icons.Filled.SelectAll, "Selecionar tudo") }
                        IconButton(onClick = { searchVisible=!searchVisible }, modifier = Modifier.testTag("btn_editor_search")) { Icon(Icons.Filled.Search, "Pesquisar") }
                    }
                }
            }
            if (searchVisible) {
                Row(Modifier.fillMaxWidth().padding(horizontal=12.dp, vertical=8.dp), verticalAlignment=Alignment.CenterVertically) {
                    OutlinedTextField(value=searchQuery, onValueChange={ searchQuery=it; searchIndex=0 }, modifier=Modifier.weight(1f).testTag("editor_search_input"), singleLine=true, label={ Text("Pesquisar") })
                    Spacer(Modifier.width(4.dp)); Text("$occurrences")
                    IconButton(onClick={ moveSearch(-1) }) { Icon(Icons.Filled.KeyboardArrowUp,"Anterior") }
                    IconButton(onClick={ moveSearch(1) }) { Icon(Icons.Filled.KeyboardArrowDown,"Próximo") }
                }
            }
            BasicTextField(
                value=value,
                onValueChange={ setEditorValue(it) },
                modifier=Modifier.fillMaxWidth().weight(1f).padding(16.dp).testTag("editor_text_field"),
                textStyle=MaterialTheme.typography.bodyLarge.copy(color=MaterialTheme.colorScheme.onBackground),
                cursorBrush=SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation=SyntaxHighlighter.visualTransformation(editorFileType),
                decorationBox={ inner -> Surface(tonalElevation=1.dp, modifier=Modifier.fillMaxSize()) { Column(Modifier.padding(12.dp)) { inner() } } }
            )
            Row(Modifier.fillMaxWidth().padding(horizontal=12.dp, vertical=6.dp), horizontalArrangement=Arrangement.SpaceBetween) {
                Text("$lines linhas • ${value.text.length} caracteres", style=MaterialTheme.typography.labelSmall)
                Text("Ln $line, Col $column", style=MaterialTheme.typography.labelSmall)
            }
        }
    }
}
