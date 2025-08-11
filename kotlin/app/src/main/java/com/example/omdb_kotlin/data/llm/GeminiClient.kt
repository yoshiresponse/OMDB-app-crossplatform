package com.example.omdb_kotlin.data.llm

import com.example.omdb_kotlin.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel

object GeminiClient {
    private val apiKey: String get() = BuildConfig.GEMINI_API_KEY

    suspend fun suggestQueries(prompt: String): List<String> {
        if (apiKey.isBlank() || apiKey == "GEMINI_API_KEY_PLACEHOLDER") return emptyList()
        val model = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
        val response = model.generateContent(
            """
            You are helping generate search queries for the OMDb API. Given a user's natural-language prompt, produce up to 3 concise movie search queries suitable for OMDb's 's' parameter.
            - Output: each query on its own line
            - No numbering, no extra commentary
            - Keep queries short (1-4 words each)

            Prompt: $prompt
            """.trimIndent()
        )
        val text = response.text ?: return emptyList()
        return text.lines()
            .map { it.trim().trimStart('-', '*').trim() }
            .filter { it.isNotEmpty() }
            .take(3)
    }
}
