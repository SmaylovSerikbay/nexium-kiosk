package com.example

import com.squareup.moshi.Moshi
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamTypeRequestTest {
  @Test
  fun createExamSendsSelectedTypeAsTypeStatus() {
    val request = CreateExamRequest(
      employeeId = "123456",
      deviceId = 4,
      typeStatus = "Предсменный",
      systolic = 120,
      diastolic = 80,
      pulse = 70,
      breathalyzer = "0.00",
      temperature = 36.6,
      complaints = "Нет",
      drugTest = "Не предусмотрено",
      deviceDopusk = "Допущен",
      priceCharged = 0.0
    )

    val json = Moshi.Builder().build().adapter(CreateExamRequest::class.java).toJson(request)

    assertTrue(json.contains("\"type_status\":\"Предсменный\""))
  }
}
