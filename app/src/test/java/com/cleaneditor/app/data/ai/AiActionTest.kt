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

    @Test
    fun taskActionFormatRequiresStructuredFields() {
        val taskInstruction = AiAction.TASK.instruction
        assertTrue(taskInstruction.contains("# Título da tarefa"))
        assertTrue(taskInstruction.contains("Prioridade:"))
        assertTrue(taskInstruction.contains("Prazo:"))
        assertTrue(taskInstruction.contains("Objetivo:"))
        assertTrue(taskInstruction.contains("Passos:"))
        assertTrue(taskInstruction.contains("- [ ]"))

        val prompt = buildAiPrompt(AiAction.TASK, "Comprar café amanhã às 9h")
        assertTrue(prompt.contains(taskInstruction))
        assertTrue(prompt.contains("Comprar café amanhã às 9h"))
    }
}
