package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class DingoSerialManagerTest {
  @Test
  fun measurementCommandMatchesSelectedTestMode() {
    assertEquals("\$STARTSENTECH\r\n", DingoSerialManager.measurementCommand(false))
    assertEquals("\$FASTSENTECH\r\n", DingoSerialManager.measurementCommand(true))
  }
}
