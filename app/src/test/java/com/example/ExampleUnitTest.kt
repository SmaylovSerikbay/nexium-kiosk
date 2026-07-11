package com.example

import org.junit.Test
import okhttp3.OkHttpClient
import okhttp3.Request

class ExampleUnitTest {
    @Test
    fun probePaths() {
        val client = OkHttpClient()
        val url = "https://nex.altiora.kz/swagger/doc.json"
        val request = Request.Builder().url(url).build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    println("=== DETAILS FOR ExamDetailResponse ===")
                    val targetKey = "\"handler.ExamDetailResponse\""
                    val index = body.indexOf(targetKey)
                    if (index != -1) {
                        val endIdx = (index + 2000).coerceAtMost(body.length)
                        println(body.substring(index, endIdx))
                    }
                }
            }
        } catch (e: Exception) {
            println("Err: ${e.message}")
        }
    }
}









