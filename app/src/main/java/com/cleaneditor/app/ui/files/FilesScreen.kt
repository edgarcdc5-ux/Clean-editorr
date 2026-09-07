package com.cleaneditor.app.ui.files

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.cleaneditor.app.R
import com.cleaneditor.app.ui.shared.CleanEditorHeader
import com.cleaneditor.app.ui.shared.EmptyStateView

@Composable
fun FilesScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_files")
    ) {
        CleanEditorHeader(
            title = stringResource(R.string.files_title),
            subtitle = "TXT • Markdown • JSON • CSV • HTML • Código • Logs"
        )

        EmptyStateView(
            icon = Icons.Filled.FolderOpen,
            title = stringResource(R.string.files_empty_title),
            description = stringResource(R.string.files_empty_desc),
            badgeText = "Fase 4 • Gerenciador de Arquivos",
            testTag = "empty_state_files"
        )
    }
}
