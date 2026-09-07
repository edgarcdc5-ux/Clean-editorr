package com.cleaneditor.app.ui.files

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.cleaneditor.app.R
import com.cleaneditor.app.data.model.FileItem
import com.cleaneditor.app.data.repository.FileManagerRepository
import com.cleaneditor.app.ui.shared.CleanEditorHeader
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

private enum class FileFilter { ALL, DOCUMENTS, FOLDERS, FAVORITES }
private enum class SortMode { NAME, DATE, SIZE }

@Composable
fun FilesScreen(onOpenFile: (FileItem) -> Unit = {}, modifier: Modifier = Modifier) {
    val context = LocalContext.current; val repository = remember { FileManagerRepository(context) }; val scope = rememberCoroutineScope()
    var currentDir by remember { mutableStateOf(repository.root()) }; var items by remember { mutableStateOf<List<FileItem>>(emptyList()) }; var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(FileFilter.ALL) }; var sortMode by remember { mutableStateOf(SortMode.NAME) }; var ascending by remember { mutableStateOf(true) }
    var dialog by remember { mutableStateOf<String?>(null) }; var selectedItem by remember { mutableStateOf<FileItem?>(null) }; var newName by remember { mutableStateOf("") }; var menuItem by remember { mutableStateOf<FileItem?>(null) }; var error by remember { mutableStateOf<String?>(null) }
    var clipboardItem by remember { mutableStateOf<FileItem?>(null) }; var clipboardMode by remember { mutableStateOf<String?>(null) }
    fun refresh() = scope.launch { items = repository.listItems(currentDir) }
    fun showError(result: Result<*>) { error = result.exceptionOrNull()?.message ?: "Operação não concluída." }
    LaunchedEffect(currentDir.absolutePath) { refresh() }
    val visibleItems = remember(items, query, filter, sortMode, ascending) {
        var result = repository.search(items, query); result = when (filter) { FileFilter.ALL -> result; FileFilter.DOCUMENTS -> result.filter { !it.isDirectory }; FileFilter.FOLDERS -> result.filter { it.isDirectory }; FileFilter.FAVORITES -> result.filter { it.isFavorite } }
        val comparator = when (sortMode) { SortMode.NAME -> compareBy<FileItem> { it.name.lowercase() }; SortMode.DATE -> compareBy { it.lastModified }; SortMode.SIZE -> compareBy { it.sizeBytes } }
        if (ascending) result.sortedWith(comparator) else result.sortedWith(comparator.reversed())
    }
    Scaffold(modifier = modifier.fillMaxSize().testTag("screen_files"), floatingActionButton = { Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) { FloatingActionButton(onClick = { newName=""; dialog="file" }, modifier=Modifier.testTag("fab_new_file")) { Icon(Icons.Filled.FileCopy,"Novo arquivo") }; FloatingActionButton(onClick={newName="";dialog="folder"}, modifier=Modifier.testTag("fab_new_folder")){Icon(Icons.Filled.CreateNewFolder,"Nova pasta")} } }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            CleanEditorHeader(title=stringResource(R.string.files_title), subtitle="Local • ${currentDir.name.ifBlank { "Documentos" }}")
            OutlinedTextField(value=query,onValueChange={query=it},modifier=Modifier.fillMaxWidth().padding(horizontal=12.dp).testTag("files_search"),singleLine=true,label={Text("Pesquisar arquivo ou extensão")})
            Row(Modifier.fillMaxWidth().padding(horizontal=8.dp,vertical=4.dp),horizontalArrangement=Arrangement.spacedBy(2.dp)){listOf(FileFilter.ALL to "Todos",FileFilter.DOCUMENTS to "Documentos",FileFilter.FOLDERS to "Pastas",FileFilter.FAVORITES to "Favoritos").forEach{(f,label)->TextButton(onClick={filter=f},modifier=Modifier.weight(1f)){Text(label,color=if(filter==f)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)}}}
            Row(Modifier.fillMaxWidth().padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically){if(currentDir.absolutePath!=repository.root().absolutePath)TextButton(onClick={currentDir=currentDir.parentFile?:repository.root()}){Text("← Voltar")};Text("${visibleItems.size} itens",style=MaterialTheme.typography.labelMedium,modifier=Modifier.weight(1f));TextButton(onClick={ascending=!ascending}){Text(if(ascending)"↑" else "↓")};TextButton(onClick={sortMode=when(sortMode){SortMode.NAME->SortMode.DATE;SortMode.DATE->SortMode.SIZE;SortMode.SIZE->SortMode.NAME}}){Text(when(sortMode){SortMode.NAME->"Nome";SortMode.DATE->"Data";SortMode.SIZE->"Tamanho"})}}
            error?.let{Text(it,color=MaterialTheme.colorScheme.error,modifier=Modifier.padding(12.dp))}
            LazyColumn(Modifier.fillMaxSize()){items(visibleItems,key={it.id}){item->FileRow(item,onClick={if(item.isDirectory)currentDir=item.file else onOpenFile(item)},onMenu={menuItem=item})}}
        }
    }
    menuItem?.let { item -> AlertDialog(onDismissRequest={menuItem=null},title={Text(item.name)},text={Column{
        TextButton(onClick={menuItem=null;if(item.isDirectory)currentDir=item.file else onOpenFile(item)}){Text("Abrir")}
        TextButton(onClick={newName=item.name;selectedItem=item;menuItem=null;dialog="rename"}){Text("Renomear")}
        TextButton(onClick={repository.toggleFavorite(item.path);menuItem=null;refresh()}){Text(if(item.isFavorite)"Desfavoritar" else "Favoritar")}
        TextButton(onClick={clipboardItem=item;clipboardMode="copy";menuItem=null}){Icon(Icons.Filled.ContentCopy,"",Modifier.padding(end=6.dp));Text("Copiar")}
        TextButton(onClick={clipboardItem=item;clipboardMode="move";menuItem=null}){Icon(Icons.Filled.DriveFileMove,"",Modifier.padding(end=6.dp));Text("Mover")}
        if(!item.isDirectory)TextButton(onClick={try{val uri=FileProvider.getUriForFile(context,"${context.packageName}.fileprovider",item.file);val intent=Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_STREAM,uri);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)};context.startActivity(Intent.createChooser(intent,"Compartilhar arquivo"))}catch(e:Exception){error=e.message?:"Não foi possível compartilhar."};menuItem=null}){Text("Compartilhar")}
        TextButton(onClick={selectedItem=item;menuItem=null;dialog="delete"}){Text("Excluir",color=MaterialTheme.colorScheme.error)}
    }},confirmButton={TextButton(onClick={menuItem=null}){Text("Fechar")}}) }
    clipboardItem?.let { source -> AlertDialog(onDismissRequest={clipboardItem=null;clipboardMode=null},title={Text(if(clipboardMode=="copy")"Copiar item" else "Mover item")},text={Column{Text("Escolha uma pasta de destino. Atualmente: ${currentDir.name.ifBlank { "Documentos" }}");Text("Use 'Colar aqui' para concluir a operação.")}},confirmButton={TextButton(onClick={scope.launch{val result=if(clipboardMode=="copy")repository.copyItem(source.file,currentDir) else repository.moveItem(source.file,currentDir);if(result.isSuccess){clipboardItem=null;clipboardMode=null;refresh()}else showError(result)}}){Text("Colar aqui")}},dismissButton={TextButton(onClick={clipboardItem=null;clipboardMode=null}){Text("Cancelar")}}) }
    when(dialog){"file"->NameDialog("Novo arquivo",newName,"Criar",{newName=it},{val fileName=if(newName.contains('.'))newName.trim() else "${newName.trim()}.txt";scope.launch{val result=repository.createFile(currentDir,fileName);if(result.isSuccess){dialog=null;refresh();result.getOrNull()?.let{file->onOpenFile(FileItem(file.absolutePath,file.name,file.absolutePath,false,file.length(),file.lastModified(),file.extension,false))}}else showError(result)}},{dialog=null});"folder"->NameDialog("Nova pasta",newName,"Criar",{newName=it},{scope.launch{val result=repository.createFolder(currentDir,newName);if(result.isSuccess){dialog=null;refresh()}else showError(result)}},{dialog=null});"rename"->NameDialog("Renomear",newName,"Salvar",{newName=it},{selectedItem?.let{item->scope.launch{val result=repository.renameItem(item.file,newName);if(result.isSuccess){dialog=null;selectedItem=null;refresh()}else showError(result)}}},{dialog=null;selectedItem=null});"delete"->AlertDialog(onDismissRequest={dialog=null;selectedItem=null},title={Text("Excluir item?")},text={Text("A exclusão de ${selectedItem?.name?:"este item"} não poderá ser desfeita.")},confirmButton={TextButton(onClick={selectedItem?.let{item->scope.launch{val result=repository.deleteItem(item.file);if(result.isSuccess){dialog=null;selectedItem=null;refresh()}else showError(result)}}}){Text("Excluir",color=MaterialTheme.colorScheme.error)}},dismissButton={TextButton(onClick={dialog=null;selectedItem=null}){Text("Cancelar")}})}
}

@Composable private fun FileRow(item:FileItem,onClick:()->Unit,onMenu:()->Unit){Row(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=6.dp).testTag("file_item_${item.name}"),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onClick){Icon(if(item.isDirectory)Icons.Filled.Folder else Icons.Filled.FileCopy,"Abrir")};Column(Modifier.weight(1f)){Text(item.name,style=MaterialTheme.typography.bodyLarge);Text(if(item.isDirectory)"Pasta" else "${formatBytes(item.sizeBytes)} • ${DateFormat.getDateInstance().format(Date(item.lastModified))}",style=MaterialTheme.typography.labelSmall)};if(item.isFavorite)Icon(Icons.Filled.Favorite,"Favorito",tint=MaterialTheme.colorScheme.primary);IconButton(onClick=onMenu){Icon(Icons.Filled.MoreVert,"Ações")}}}
@Composable private fun NameDialog(title:String,value:String,confirm:String,onNameChange:(String)->Unit,onConfirm:()->Unit,onDismiss:()->Unit){AlertDialog(onDismissRequest=onDismiss,title={Text(title)},text={OutlinedTextField(value=value,onValueChange=onNameChange,singleLine=true,label={Text("Nome")})},confirmButton={Button(onClick=onConfirm,enabled=value.trim().isNotEmpty()){Text(confirm)}},dismissButton={TextButton(onClick=onDismiss){Text("Cancelar")}})}
private fun formatBytes(bytes:Long):String=when{bytes<1024->"$bytes B";bytes<1024*1024->"${bytes/1024} KB";else->"${bytes/(1024*1024)} MB"}
