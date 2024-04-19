package dev.shiron.kotodiscord.command.translation

import com.google.gson.JsonParser
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

fun translateText(
    apiKey: String,
    text: String,
    targetLang: String,
    sourceLang: String? = null,
): String {
    val client = OkHttpClient()
    var builder =
        FormBody.Builder()
            .add("auth_key", apiKey)
            .add("text", text)
            .add("target_lang", targetLang)
    if (sourceLang != null) {
        builder = builder.add("source_lang", sourceLang)
    }
    val requestBody = builder.build()

    val request =
        Request.Builder()
            .url("https://api-free.deepl.com/v2/translate")
            .post(requestBody)
            .build()

    client.newCall(request).execute().use { response ->
        if (response.isSuccessful) {
            val jsonResponse = response.body?.string() ?: throw IllegalStateException("Response body is null")
            val jsonObject = JsonParser.parseString(jsonResponse).asJsonObject
            val translations = jsonObject.getAsJsonArray("translations")
            val firstTranslation = translations.get(0).asJsonObject
            return firstTranslation.get("text").asString
        } else {
            throw RuntimeException("Failed to translate text: ${response.code}")
        }
    }
}
