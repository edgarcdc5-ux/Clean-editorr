package com.cleaneditor.app.data.ai

import org.junit.Assert.assertTrue
import org.junit.Test

class AiActionTest {
    @Test
    fun buildAiPromptIncludesInstructionAndText() {
        val prompt = buildAiPrompt(AiAction.CORRECT, "Texto com erro")

        assertTrue(prompt.contains("Corrija ortografia"))
        assertTrue(prompt.contains("Texto de referência:"))
        assertTrue(prompt.contains("Texto com erro"))
    }

    @Test
    fun allQuickActionsHaveInstructions() {
        AiAction.values().forEach { action ->
            assertTrue(action.instruction.isNotBlank())
        }
    }
}
