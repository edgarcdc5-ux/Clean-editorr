package com.cleaneditor.app.ui.reminders

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.cleaneditor.app.R
import com.cleaneditor.app.ui.shared.CleanEditorHeader
import com.cleaneditor.app.ui.shared.EmptyStateView

@Composable
fun RemindersScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_reminders")
    ) {
        CleanEditorHeader(
            title = stringResource(R.string.reminders_title),
            subtitle = "Tarefas, datas, horários, categorias e prioridades"
        )

        EmptyStateView(
            icon = Icons.Filled.NotificationsActive,
            title = stringResource(R.string.reminders_empty_title),
            description = stringResource(R.string.reminders_empty_desc),
            badgeText = "Fase 5 • Lembretes e Tarefas",
            testTag = "empty_state_reminders"
        )
    }
}
