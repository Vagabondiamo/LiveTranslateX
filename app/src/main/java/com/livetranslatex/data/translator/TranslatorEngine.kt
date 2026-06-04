package com.livetranslatex.data.translator

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class TranslationEngine { MLKIT, DEEPL, OPENAI }

@Singleton
class TranslatorEngine @Inject constructor() {

    private val httpClient = OkHttpClient()
    private val mlKitTranslators = mutableMapOf<String, com.google.mlkit.nl.translate.Translator>()

    var engine = TranslationEngine.MLKIT
    var sourceLang = "ja"
    var targetLang = "it"
    var deeplApiKey = ""
    var openAiApiKey = ""

    suspend fun translate(text: String): String {
        if (text.isBlank()) return ""
        return try {
            when (engine) {
                TranslationEngine.MLKIT -> translateMlKit(text)
                TranslationEngine.DEEPL -> translateDeepL(text)
                TranslationEngine.OPENAI -> translateOpenAI(text)
            }
        } catch (e: Exception) {
            // Fallback a ML Kit se gli altri falliscono
            try { translateMlKit(text) } catch (e2: Exception) { text }
        }
    }

    // ── ML Kit (offline) ──────────────────────────────────────────────────────

    private suspend fun translateMlKit(text: String): String {
        val key = "${sourceLang}_${targetLang}"
        val translator = mlKitTranslators.getOrPut(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(toMlKitLang(sourceLang))
                .setTargetLanguage(toMlKitLang(targetLang))
                .build()
            Translation.getClient(options).also { t ->
                t.downloadModelIfNeeded().await()
            }
        }
        return translator.translate(text).await()
    }

    private fun toMlKitLang(lang: String) = when (lang.lowercase()) {
        "it" -> TranslateLanguage.ITALIAN
        "en" -> TranslateLanguage.ENGLISH
        "ja" -> TranslateLanguage.JAPANESE
        "zh" -> TranslateLanguage.CHINESE
        "ko" -> TranslateLanguage.KOREAN
        "fr" -> TranslateLanguage.FRENCH
        "es" -> TranslateLanguage.SPANISH
        "de" -> TranslateLanguage.GERMAN
        else -> TranslateLanguage.ENGLISH
    }

    // ── DeepL ─────────────────────────────────────────────────────────────────

    private suspend fun translateDeepL(text: String): String {
        if (deeplApiKey.isBlank()) throw IllegalStateException("DeepL API key mancante")

        val body = FormBody.Builder()
            .add("auth_key", deeplApiKey)
            .add("text", text)
            .add("source_lang", sourceLang.uppercase())
            .add("target_lang", targetLang.uppercase())
            .build()

        val request = Request.Builder()
            .url("https://api-free.deepl.com/v2/translate")
            .post(body)
            .build()

        return suspendCall(request) { responseBody ->
            val json = JSONObject(responseBody)
            json.getJSONArray("translations").getJSONObject(0).getString("text")
        }
    }

    // ── OpenAI ────────────────────────────────────────────────────────────────

    private suspend fun translateOpenAI(text: String): String {
        if (openAiApiKey.isBlank()) throw IllegalStateException("OpenAI API key mancante")

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("value", "Sei un traduttore professionale. Traduci il testo da $sourceLang a $targetLang. Rispondi solo con la traduzione, nessun altro testo.")
            })
            put(JSONObject().apply {
                put("role", "user")
                put("value", text)
            })
        }

        val jsonBody = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", messages)
            put("max_tokens", 500)
        }

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer $openAiApiKey")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return suspendCall(request) { responseBody ->
            val json = JSONObject(responseBody)
            json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private suspend fun <T> suspendCall(request: Request, parse: (String) -> T): T {
        return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            val call = httpClient.newCall(request)
            cont.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resumeWith(Result.failure(e))
                }
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string() ?: ""
                    if (!response.isSuccessful) {
                        cont.resumeWith(Result.failure(IOException("HTTP ${response.code}: $body")))
                    } else {
                        try {
                            cont.resumeWith(Result.success(parse(body)))
                        } catch (e: Exception) {
                            cont.resumeWith(Result.failure(e))
                        }
                    }
                }
            })
        }
    }
}
