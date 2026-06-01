package com.livetranslatex.data.translator

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import javax.inject.Inject

interface Translator {
    suspend fun translate(text: String, source: String, target: String): String
}

// ── Offline: ML Kit ──────────────────────────────────────────────────────────

class MlKitTranslator @Inject constructor() : Translator {

    private val translators = mutableMapOf<String, com.google.mlkit.nl.translate.Translator>()

    override suspend fun translate(text: String, source: String, target: String): String {
        val key = "${source}_$target"
        val translator = translators.getOrPut(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.fromLanguageTag(source) ?: TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.fromLanguageTag(target) ?: TranslateLanguage.ITALIAN)
                .build()
            Translation.getClient(options).also { t ->
                t.downloadModelIfNeeded().await()
            }
        }
        return translator.translate(text).await()
    }
}

// ── Online: DeepL ────────────────────────────────────────────────────────────

interface DeepLApi {
    @POST("v2/translate")
    suspend fun translate(
        @Header("Authorization") auth: String,
        @Body body: DeepLRequest
    ): DeepLResponse
}

data class DeepLRequest(val text: List<String>, val source_lang: String, val target_lang: String)
data class DeepLResponse(val translations: List<DeepLTranslation>)
data class DeepLTranslation(val text: String)

class DeepLTranslator @Inject constructor(
    private val api: DeepLApi,
    private val apiKey: String
) : Translator {
    override suspend fun translate(text: String, source: String, target: String): String {
        val response = api.translate(
            auth = "DeepL-Auth-Key $apiKey",
            body = DeepLRequest(
                text = listOf(text),
                source_lang = source.uppercase(),
                target_lang = target.uppercase()
            )
        )
        return response.translations.firstOrNull()?.text ?: text
    }
}

// ── AI: OpenAI ───────────────────────────────────────────────────────────────

interface OpenAiApi {
    @POST("v1/chat/completions")
    suspend fun complete(
        @Header("Authorization") auth: String,
        @Body body: OpenAiRequest
    ): OpenAiResponse
}

data class OpenAiRequest(val model: String, val messages: List<OpenAiMessage>, val max_tokens: Int = 500)
data class OpenAiMessage(val role: String, val content: String)
data class OpenAiResponse(val choices: List<OpenAiChoice>)
data class OpenAiChoice(val message: OpenAiMessage)

class OpenAiTranslator @Inject constructor(
    private val api: OpenAiApi,
    private val apiKey: String
) : Translator {
    override suspend fun translate(text: String, source: String, target: String): String {
        val prompt = "Translate the following text from $source to $target. Return only the translation, no explanations:\n\n$text"
        val response = api.complete(
            auth = "Bearer $apiKey",
            body = OpenAiRequest(
                model = "gpt-4o-mini",
                messages = listOf(OpenAiMessage("user", prompt))
            )
        )
        return response.choices.firstOrNull()?.message?.content?.trim() ?: text
    }
}
