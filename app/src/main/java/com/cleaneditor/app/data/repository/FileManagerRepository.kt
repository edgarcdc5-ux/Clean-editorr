package com.cleaneditor.app.data.repository

import android.content.Context
import com.cleaneditor.app.data.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class FileManagerRepository(context: Context) {
    private val rootDir = File(context.filesDir, "documents").apply { mkdirs() }
    private val prefs = context.getSharedPreferences("cleaneditor_files", Context.MODE_PRIVATE)

    suspend fun listItems(currentDir: File = rootDir): List<FileItem> = withContext(Dispatchers.IO) {
        val dir = safeDirectory(currentDir)
        dir.listFiles()?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() })?.map(::toItem).orEmpty()
    }

    suspend fun createFolder(parent: File, folderName: String): Result<File> = withContext(Dispatchers.IO) {
        validateName(folderName).fold(onSuccess = {
            val target = safeChild(parent, folderName)
            if (target.exists()) Result.failure(IOException("Já existe um item com esse nome."))
            else if (target.mkdirs()) Result.success(target) else Result.failure(IOException("Não foi possível criar a pasta."))
        }, onFailure = { Result.failure(it) })
    }

    suspend fun createFile(parent: File, fileName: String, content: String = ""): Result<File> = withContext(Dispatchers.IO) {
        validateName(fileName).fold(onSuccess = {
            val target = safeChild(parent, fileName)
            if (target.exists()) Result.failure(IOException("Já existe um item com esse nome."))
            else try { target.parentFile?.mkdirs(); target.writeText(content, Charsets.UTF_8); Result.success(target) }
            catch (e: IOException) { Result.failure(e) }
        }, onFailure = { Result.failure(it) })
    }

    suspend fun renameItem(item: File, newName: String): Result<File> = withContext(Dispatchers.IO) {
        validateName(newName).fold(onSuccess = {
            val source = safeExisting(item); val target = safeChild(source.parentFile ?: rootDir, newName)
            if (target.exists()) Result.failure(IOException("Já existe um item com esse nome."))
            else if (source.renameTo(target)) Result.success(target) else Result.failure(IOException("Não foi possível renomear o item."))
        }, onFailure = { Result.failure(it) })
    }

    suspend fun deleteItem(item: File): Result<Boolean> = withContext(Dispatchers.IO) {
        try { Result.success(safeExisting(item).deleteRecursively()) } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun copyItem(source: File, destinationDir: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            val src = safeExisting(source); val dir = safeDirectory(destinationDir)
            require(src != dir) { "Origem e destino inválidos." }
            val target = uniqueTarget(dir, src.name)
            if (src.isDirectory) copyDirectory(src, target) else src.copyTo(target, overwrite = false)
            Result.success(target)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun moveItem(source: File, destinationDir: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            val src = safeExisting(source); val dir = safeDirectory(destinationDir)
            require(!dir.toPath().startsWith(src.canonicalFile.toPath())) { "Não é possível mover uma pasta para dentro dela mesma." }
            val target = uniqueTarget(dir, src.name)
            if (!src.renameTo(target)) {
                if (src.isDirectory) copyDirectory(src, target) else src.copyTo(target, overwrite = false)
                if (!src.deleteRecursively()) throw IOException("Cópia criada, mas não foi possível remover a origem.")
            }
            Result.success(target)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun readFile(file: File): Result<String> = withContext(Dispatchers.IO) {
        try { Result.success(safeExisting(file).readText(Charsets.UTF_8)) } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun writeFile(file: File, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try { safeExistingOrChild(file).writeText(content, Charsets.UTF_8); Result.success(Unit) } catch (e: Exception) { Result.failure(e) }
    }

    fun toggleFavorite(filePath: String): Boolean {
        val favorites = prefs.getStringSet("favorites", emptySet()).orEmpty().toMutableSet()
        val normalized = File(filePath).absolutePath
        val nowFavorite = if (favorites.contains(normalized)) { favorites.remove(normalized); false } else { favorites.add(normalized); true }
        prefs.edit().putStringSet("favorites", favorites).apply(); return nowFavorite
    }

    fun isFavorite(filePath: String): Boolean = prefs.getStringSet("favorites", emptySet()).orEmpty().contains(File(filePath).absolutePath)
    fun root(): File = rootDir
    fun search(items: List<FileItem>, query: String): List<FileItem> = if (query.isBlank()) items else items.filter { it.name.contains(query, true) || it.extension.contains(query.trimStart('.'), true) }

    private fun toItem(file: File) = FileItem(file.absolutePath, file.name, file.absolutePath, file.isDirectory, if (file.isFile) file.length() else 0L, file.lastModified(), if (file.isFile) file.extension.lowercase() else "", isFavorite(file.absolutePath))
    private fun safeDirectory(dir: File): File { val candidate = dir.canonicalFile; require(candidate == rootDir.canonicalFile || candidate.toPath().startsWith(rootDir.canonicalFile.toPath())) { "Diretório inválido." }; if (!candidate.exists()) candidate.mkdirs(); require(candidate.isDirectory) { "Não é uma pasta." }; return candidate }
    private fun safeExisting(file: File): File { val candidate = file.canonicalFile; require(candidate.toPath().startsWith(rootDir.canonicalFile.toPath()) && candidate != rootDir.canonicalFile) { "Arquivo inválido." }; require(candidate.exists()) { "Arquivo não encontrado." }; return candidate }
    private fun safeExistingOrChild(file: File): File { val candidate = file.canonicalFile; require(candidate.toPath().startsWith(rootDir.canonicalFile.toPath()) && candidate != rootDir.canonicalFile) { "Arquivo inválido." }; return candidate }
    private fun safeChild(parent: File, name: String): File { val safeParent = safeDirectory(parent); val target = File(safeParent, name).canonicalFile; require(target.parentFile == safeParent.canonicalFile) { "Nome de arquivo inválido." }; return target }
    private fun uniqueTarget(parent: File, name: String): File { var candidate = File(parent, name); if (!candidate.exists()) return candidate; val ext = name.substringAfterLast('.', ""); val base = if (ext.isEmpty()) name else name.removeSuffix(".$ext"); var index = 1; do { candidate = File(parent, if (ext.isEmpty()) "$base ($index)" else "$base ($index).$ext"); index++ } while (candidate.exists()); return candidate }
    private fun copyDirectory(source: File, target: File) { require(target.mkdirs()) { "Não foi possível criar o destino." }; source.listFiles()?.forEach { child -> val childTarget = File(target, child.name); if (child.isDirectory) copyDirectory(child, childTarget) else child.copyTo(childTarget, overwrite = false) } }

    companion object {
        fun validateName(name: String): Result<Unit> { val trimmed = name.trim(); return when { trimmed.isEmpty() -> Result.failure(IllegalArgumentException("O nome não pode ficar vazio.")); trimmed == "." || trimmed == ".." -> Result.failure(IllegalArgumentException("Nome inválido.")); trimmed.contains('/') || trimmed.contains('\\') -> Result.failure(IllegalArgumentException("O nome contém caracteres inválidos.")); else -> Result.success(Unit) } }
    }
}
