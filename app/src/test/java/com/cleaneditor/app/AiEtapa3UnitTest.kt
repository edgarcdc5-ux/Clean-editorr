package com.cleaneditor.app

import com.cleaneditor.app.data.ai.AiAction
import com.cleaneditor.app.data.ai.AiHistoryEntry
import com.cleaneditor.app.data.ai.AiHistoryStore
import com.cleaneditor.app.data.ai.buildAiPrompt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiEtapa3UnitTest {

    @Test
    fun historyKeepsNewestThirtyEntries() {
        val existing = (1L..30L).map { index ->
            AiHistoryEntry(index, "GERAR", "prompt $index", "response $index")
        }
        val result = AiHistoryStore.trimEntries(existing, AiHistoryEntry(31L, "CORRECT", "novo", "resultado"))

        assertEquals(30, result.size)
        assertEquals(31L, result.first().timestamp)
        assertTrue(result.any { it.timestamp == 30L })
        assertTrue(result.none { it.timestamp == 1L })
    }

    @Test
    fun quickActionPromptContainsInstructionAndReferenceText() {
        val prompt = buildAiPrompt(AiAction.CORRECT, "  Texto de teste.  ")

        assertTrue(prompt.contains(AiAction.CORRECT.instruction))
        assertTrue(prompt.contains("Texto de referência:"))
        assertTrue(prompt.endsWith("Texto de teste."))
    }

    @Test
    fun taskActionRequiresStructuredOutput() {
        assertTrue(AiAction.TASK.instruction.contains("Prioridade"))
        assertTrue(AiAction.TASK.instruction.contains("Prazo"))
        assertTrue(AiAction.TASK.instruction.contains("- [ ]"))
    }
}
