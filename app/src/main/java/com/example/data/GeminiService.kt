package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Returns if API key is present and is not the default placeholder
    fun isApiKeyAvailable(): Boolean {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && !key.startsWith("placeholder")
        } catch (e: Exception) {
            false
        }
    }

    suspend fun explainConcept(subject: String, chapter: String, conceptTitle: String, conceptContent: String): String = withContext(Dispatchers.IO) {
        if (!isApiKeyAvailable()) {
            return@withContext "API key not configured. Please enter your GEMINI_API_KEY in the AI Studio Secrets panel."
        }

        val prompt = """
            You are an expert tutor. Please explain this study concept simply using 3 clear, highly educational, easy-to-read bullet points under 100 words in total.
            
            Subject: $subject
            Chapter: $chapter
            Concept: $conceptTitle
            Details: $conceptContent
        """.trimIndent()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${BuildConfig.GEMINI_API_KEY}"
        
        val jsonPayload = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().put("parts", JSONArray().put(
                    JSONObject().put("text", prompt)
                ))
            ))
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonPayload.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext "Error: Failed to fetch explanation from AI (${response.code})."
                val bodyString = response.body?.string() ?: return@withContext "Error: Empty response body."
                
                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates")
                val parts = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")
                text ?: "No explanation found. Please try again."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini", e)
            "Could not connect to AI service: ${e.message}"
        }
    }

    suspend fun generateCustomQuiz(subject: String, chapter: String): List<QuizQuestion> = withContext(Dispatchers.IO) {
        if (!isApiKeyAvailable()) {
            return@withContext emptyList()
        }

        val prompt = """
            You are a science and math board-exam question paper designer. Create critical practice questions.
            Generate exactly 3 multiple choice questions for the subject "$subject", chapter "$chapter" in JSON format.
            Return a JSON array where each object has these EXACT keys:
            "id": integer (1, 2, 3),
            "question": string (the question text),
            "options": array of 4 strings,
            "correctIndex": integer (0 to 3),
            "explanation": string (why this option is correct).
            
            Return ONLY the raw JSON array. Do not enclose it in markdown blocks. Do not write introductory words.
        """.trimIndent()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${BuildConfig.GEMINI_API_KEY}"
        
        val jsonPayload = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().put("parts", JSONArray().put(
                    JSONObject().put("text", prompt)
                ))
            ))
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonPayload.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val bodyString = response.body?.string() ?: return@withContext emptyList()
                
                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates")
                val parts = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                var text = parts?.optJSONObject(0)?.optString("text")?.trim() ?: return@withContext emptyList()

                // Stripping markdown wrapper if AI returned it despite instructions
                if (text.startsWith("```json")) {
                    text = text.substringAfter("```json").substringBeforeLast("```")
                } else if (text.startsWith("```")) {
                    text = text.substringAfter("```").substringBeforeLast("```")
                }
                
                val questionsArray = JSONArray(text.trim())
                val resultList = mutableListOf<QuizQuestion>()
                for (i in 0 until questionsArray.length()) {
                    val obj = questionsArray.getJSONObject(i)
                    val id = obj.optInt("id", i + 1)
                    val question = obj.optString("question")
                    val optionsArr = obj.getJSONArray("options")
                    val options = List(optionsArr.length()) { optionsArr.getString(it) }
                    val correctIndex = obj.optInt("correctIndex", 0)
                    val explanation = obj.optString("explanation")

                    resultList.add(
                        QuizQuestion(
                            id = id,
                            question = question,
                            options = options,
                            correctIndex = correctIndex,
                            explanation = explanation
                        )
                    )
                }
                resultList
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception parsing Gemini Custom Quiz response", e)
            emptyList()
        }
    }
}
