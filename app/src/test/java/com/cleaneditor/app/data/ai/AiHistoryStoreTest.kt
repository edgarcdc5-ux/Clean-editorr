package com.cleaneditor.app.data.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiHistoryStoreTest {

    @Test
    fun maxEntriesIsThirty() {
        assertEquals(30, AiHistoryStore.MAX_ENTRIES)
    }

    @Test
    fun trimEntriesEnforcesMaxThirtyItems() {
        val existing = (1..30).map { i ->
            AiHistoryEntry(
                timestamp = i.toLong(),
                action = "GERAR",
                prompt = "Prompt $i",
                response = "Response $i"
            )
        }

        val newEntry = AiHistoryEntry(
            timestamp = 999L,
            action = "SUMMARIZE",
            prompt = "Novo prompt",
            response = "Nova resposta"
        )

        val trimmed = AiHistoryStore.trimEntries(existing, newEntry)

        assertEquals(30, trimmed.size)
        assertEquals(newEntry, trimmed.first())
        assertEquals("Prompt 1", trimmed[1].prompt)
        // The 30th original item (Prompt 30) was dropped
        assertTrue(trimmed.none { it.prompt == "Prompt 30" })
    }

    @Test
    fun trimEntriesWithFewerThanThirtyGrowsNormally() {
        val existing = listOf(
            AiHistoryEntry(1L, "CORRECT", "P1", "R1")
        )
        val newEntry = AiHistoryEntry(2L, "REWRITE", "P2", "R2")

        val result = AiHistoryStore.trimEntries(existing, newEntry)

        assertEquals(2, result.size)
        assertEquals(newEntry, result[0])
        assertEquals(existing[0], result[1])
    }
}
