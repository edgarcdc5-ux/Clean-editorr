package com.cleaneditor.app.data.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiServiceTest {
    private val service = GeminiService()

    @Test
    fun promptExceedingMaxLengthFails() = runBlocking {
        val longPrompt = "a".repeat(GeminiService.MAX_PROMPT_CHARS + 1)
        val result = service.generate(longPrompt)

        assertTrue(result.isFailure)
        val message = result.exceptionOrNull()?.message.orEmpty()
        assertTrue(message.contains("O texto é muito longo"))
        assertTrue(message.contains(GeminiService.MAX_PROMPT_CHARS.toString()))
    }

    @Test
    fun emptyPromptFails() = runBlocking {
        val result = service.generate("   \n\t  ")

        assertTrue(result.isFailure)
        val message = result.exceptionOrNull()?.message.orEmpty()
        assertTrue(message.contains("Digite um texto antes de consultar a IA"))
    }

    @Test
    fun emptyApiResponseFailsWithClearMessage() {
        val emptyTextResponse = """
            {
                "candidates": [
                    {
                        "content": {
                            "parts": [
                                {
                                    "text": "   "
                                }
                            ]
                        }
                    }
                ]
            }
        """.trimIndent()

        val result = service.parseResponse(emptyTextResponse)
        assertTrue(result.isFailure)
        assertEquals("O Gemini retornou uma resposta vazia.", result.exceptionOrNull()?.message)
    }

    @Test
    fun missingCandidatesFails() {
        val noCandidatesResponse = """
            {
                "candidates": []
            }
        """.trimIndent()

        val result = service.parseResponse(noCandidatesResponse)
        assertTrue(result.isFailure)
    }

    @Test
    fun missingContentFails() {
        val result = service.parseResponse("""{"candidates":[{}]}""")
        assertTrue(result.isFailure)
        assertEquals("Resposta do Gemini sem conteúdo.", result.exceptionOrNull()?.message)
    }

    @Test
    fun multipleResponsePartsAreCombined() {
        val response = """
            {
                "candidates": [
                    {
                        "content": {
                            "parts": [
                                {"text": "Primeira parte "},
                                {"text": "e segunda parte."}
                            ]
                        }
                    }
                ]
            }
        """.trimIndent()

        val result = service.parseResponse(response)
        assertTrue(result.isSuccess)
        assertEquals("Primeira parte e segunda parte.", result.getOrNull())
    }

    @Test
    fun validApiResponseReturnsExtractedText() {
        val validResponse = """
            {
                "candidates": [
                    {
                        "content": {
                            "parts": [
                                {
                                    "text": "Resumo do documento com sucesso."
                                }
                            ]
                        }
                    }
                ]
            }
        """.trimIndent()

        val result = service.parseResponse(validResponse)
        assertTrue(result.isSuccess)
        assertEquals("Resumo do documento com sucesso.", result.getOrNull())
    }
}
