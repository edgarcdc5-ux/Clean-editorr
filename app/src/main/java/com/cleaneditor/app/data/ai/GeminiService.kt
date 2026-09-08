package com.cleaneditor.app.data.ai

import com.cleaneditor.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GeminiService {
    suspend fun generate(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
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
                    .put("contents", org.json.JSONArray().put(
                        JSONObject().put("parts", org.json.JSONArray().put(JSONObject().put("text", prompt)))
                    ))
                    .toString()
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val response = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
                if (code !in 200..299) {
                    val message = runCatching { JSONObject(response).optJSONObject("error")?.optString("message") }.getOrNull()
                    error(message?.takeIf { it.isNotBlank() } ?: "Falha na API do Gemini (HTTP $code).")
                }

                val json = JSONObject(response)
                val candidates = json.optJSONArray("candidates") ?: error("Resposta do Gemini sem candidatos.")
                val text = candidates.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    ?.trim()
                    .orEmpty()
                text.ifBlank { error("O Gemini retornou uma resposta vazia.") }
            } finally {
                connection.disconnect()
            }
        }
    }
}
