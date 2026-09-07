package com.cleaneditor.app.ui.ai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.cleaneditor.app.R
import com.cleaneditor.app.ui.shared.CleanEditorHeader
import com.cleaneditor.app.ui.shared.EmptyStateView

@Composable
fun AiScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_ai")
    ) {
        CleanEditorHeader(
            title = stringResource(R.string.ai_title),
            subtitle = "Resumos, revisão, tradução e extração de tarefas"
        )

        EmptyStateView(
            icon = Icons.Filled.Psychology,
            title = stringResource(R.string.ai_empty_title),
            description = stringResource(R.string.ai_empty_desc),
            badgeText = "Fase 6 • Integração com Gemini",
            testTag = "empty_state_ai"
        )
    }
}
