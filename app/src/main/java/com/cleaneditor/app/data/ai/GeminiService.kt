package com.cleaneditor.app.data.ai

import com.cleaneditor.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GeminiService {
    companion object {
        const val MAX_PROMPT_CHARS = 20_000
        const val MODEL = "gemini-3.6-flash"
        private const val MAX_OUTPUT_TOKENS = 2_048
        private const val MAX_ATTEMPTS = 2
    }

    suspend fun generate(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanPrompt = prompt.trim()
            require(cleanPrompt.isNotEmpty()) { "Digite um texto antes de consultar a IA." }
            require(cleanPrompt.length <= MAX_PROMPT_CHARS) {
                "O texto é muito longo. Limite: $MAX_PROMPT_CHARS caracteres."
            }
            val apiKey = BuildConfig.GEMINI_API_KEY.trim()
            require(apiKey.isNotEmpty() && apiKey != "your_api_key_here" && !apiKey.contains("your_api_key")) {
                "Chave da API do Gemini não configurada."
            }

            var lastError: Throwable? = null
            repeat(MAX_ATTEMPTS) { attempt ->
                try {
                    return@runCatching request(cleanPrompt, apiKey)
                } catch (throwable: Throwable) {
                    lastError = throwable
                    if (attempt + 1 < MAX_ATTEMPTS && throwable is IOException) {
                        delay(600L)
                    } else {
                        throw throwable
                    }
                }
            }
            throw lastError ?: IOException("Não foi possível conectar à IA.")
        }.recoverCatching { throwable ->
            when (throwable) {
                is IOException -> error("Não foi possível conectar à IA. Verifique sua internet e tente novamente.")
                else -> throw throwable
            }
        }
    }

    private fun request(prompt: String, apiKey: String): String {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 45_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("x-goog-api-key", apiKey)
        }
        try {
            val generationConfig = JSONObject()
                .put("maxOutputTokens", MAX_OUTPUT_TOKENS)
            val body = JSONObject()
                .put("contents", JSONArray().put(
                    JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                ))
                .put("generationConfig", generationConfig)
                .toString()
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.let {
                BufferedReader(InputStreamReader(it, Charsets.UTF_8)).use { reader -> reader.readText() }
            }.orEmpty()
            if (code !in 200..299) {
                val apiMessage = runCatching {
                    JSONObject(response).optJSONObject("error")?.optString("message")
                }.getOrNull()
                val detail = apiMessage?.takeIf { it.isNotBlank() } ?: "Falha na API do Gemini."
                error(when (code) {
                    401, 403 -> "Chave do Gemini inválida ou sem permissão."
                    429 -> "Limite de uso da IA atingido. Tente novamente mais tarde."
                    in 500..599 -> "O serviço do Gemini está indisponível no momento. Tente novamente."
                    else -> "$detail (HTTP $code)"
                })
            }
            return parseResponse(response).getOrThrow()
        } finally {
            connection.disconnect()
        }
    }

    fun parseResponse(response: String): Result<String> = runCatching {
        val json = JSONObject(response)
        val candidates = json.optJSONArray("candidates") ?: error("Resposta do Gemini sem candidatos.")
        val parts = candidates.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
            ?: error("Resposta do Gemini sem conteúdo.")
        val text = buildString {
            for (index in 0 until parts.length()) {
                parts.optJSONObject(index)?.optString("text")?.let { append(it) }
            }
        }.trim()
        text.ifBlank { error("O Gemini retornou uma resposta vazia.") }
    }
}
