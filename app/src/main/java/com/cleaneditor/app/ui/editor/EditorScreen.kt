package com.cleaneditor.app.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var value by remember { mutableStateOf(TextFieldValue("")) }
    var savedText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }
    var searchIndex by remember { mutableStateOf(0) }
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
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sem título" + if (isModified) " •" else "",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (isModified) "Documento modificado" else "Documento não salvo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(onClick = {
                            setEditorValue(TextFieldValue(""))
                            savedText = ""
                        }) { Icon(Icons.Filled.Add, "Novo") }
                        IconButton(onClick = {
                            scope.launch { snackbarHostState.showSnackbar("Abrir arquivo será conectado na próxima fase.") }
                        }) { Icon(Icons.Filled.FolderOpen, "Abrir") }
                        IconButton(onClick = {
                            savedText = value.text
                            scope.launch { snackbarHostState.showSnackbar("Documento marcado como salvo localmente.") }
                        }) { Icon(Icons.Filled.Save, "Salvar") }
                        IconButton(enabled = undoStack.isNotEmpty(), onClick = {
                            if (undoStack.isNotEmpty()) {
                                redoStack.add(value)
                                value = undoStack.removeAt(undoStack.lastIndex)
                            }
                        }) { Icon(Icons.Filled.Undo, "Desfazer") }
                        IconButton(enabled = redoStack.isNotEmpty(), onClick = {
                            if (redoStack.isNotEmpty()) {
                                undoStack.add(value)
                                value = redoStack.removeAt(redoStack.lastIndex)
                            }
                        }) { Icon(Icons.Filled.Redo, "Refazer") }
                        IconButton(onClick = {
                            clipboard.setText(AnnotatedString(value.text.substring(value.selection.start, value.selection.end)))
                        }) { Icon(Icons.Filled.ContentCopy, "Copiar") }
                        IconButton(onClick = {
                            val selected = value.text.substring(value.selection.start, value.selection.end)
                            clipboard.setText(AnnotatedString(selected))
                            setEditorValue(
                                value.copy(
                                    text = value.text.removeRange(value.selection.start, value.selection.end),
                                    selection = TextRange(value.selection.start)
                                )
                            )
                        }) { Icon(Icons.Filled.ContentCut, "Recortar") }
                        IconButton(onClick = {
                            val pasted = clipboard.getText()?.text ?: return@IconButton
                            setEditorValue(
                                value.copy(
                                    text = value.text.replaceRange(value.selection.start, value.selection.end, pasted),
                                    selection = TextRange(value.selection.start + pasted.length)
                                )
                            )
                        }) { Icon(Icons.Filled.ContentPaste, "Colar") }
                        IconButton(onClick = {
                            setEditorValue(value.copy(selection = TextRange(0, value.text.length)), recordHistory = false)
                        }) { Icon(Icons.Filled.SelectAll, "Selecionar tudo") }
                        IconButton(onClick = { searchVisible = !searchVisible }) {
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
                        modifier = Modifier.weight(1f),
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
