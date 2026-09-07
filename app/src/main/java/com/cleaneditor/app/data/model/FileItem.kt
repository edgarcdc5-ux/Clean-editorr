package com.cleaneditor.app.data.model

import java.io.File

/** A file-system item managed by CleanEditor. */
data class FileItem(
    val id: String,
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long,
    val extension: String,
    val isFavorite: Boolean
) {
    val file: File get() = File(path)
}
