package com.cleaneditor.app.data.ai

import com.cleaneditor.app.BuildConfig
import kotlinx.coroutines.Dispatchers
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
    }

    suspend fun generate(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanPrompt = prompt.trim()
            require(cleanPrompt.isNotEmpty()) { "Digite um texto antes de consultar a IA." }
            require(cleanPrompt.length <= MAX_PROMPT_CHARS) { "O texto é muito longo. Limite: $MAX_PROMPT_CHARS caracteres." }

            val apiKey = BuildConfig.GEMINI_API_KEY.trim()
            require(apiKey.isNotEmpty()) { "Chave da API do Gemini não configurada." }

            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 45_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }

            try {
                val body = JSONObject()
                    .put("contents", JSONArray().put(JSONObject().put("parts", JSONArray().put(JSONObject().put("text", cleanPrompt)))))
                    .toString()
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val response = stream?.let { BufferedReader(InputStreamReader(it, Charsets.UTF_8)).use(BufferedReader::readText) }.orEmpty()
                if (code !in 200..299) {
                    val apiMessage = runCatching { JSONObject(response).optJSONObject("error")?.optString("message") }.getOrNull()
                    val detail = apiMessage?.takeIf { it.isNotBlank() } ?: "Falha na API do Gemini."
                    error(when (code) {
                        401, 403 -> "Chave do Gemini inválida ou sem permissão."
                        429 -> "Limite de uso da IA atingido. Tente novamente mais tarde."
                        in 500..599 -> "O serviço do Gemini está indisponível no momento. Tente novamente."
                        else -> "$detail (HTTP $code)"
                    })
                }

                val json = JSONObject(response)
                val candidates = json.optJSONArray("candidates") ?: error("Resposta do Gemini sem candidatos.")
                val text = candidates.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")?.trim().orEmpty()
                text.ifBlank { error("O Gemini retornou uma resposta vazia.") }
            } finally {
                connection.disconnect()
            }
        }.recoverCatching { throwable ->
            when (throwable) {
                is IOException -> error("Não foi possível conectar à IA. Verifique sua internet e tente novamente.")
                else -> throw throwable
            }
        }
    }
}
