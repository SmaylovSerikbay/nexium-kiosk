package com.example

import android.provider.Settings
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class WifiSettingsIntentTest {

  @Test
  @Config(sdk = [28])
  fun `old Android opens Wi-Fi settings`() {
    assertEquals(Settings.ACTION_WIFI_SETTINGS, KioskManager.wifiSettingsIntent().action)
  }

  @Test
  @Config(sdk = [29])
  fun `Android 10 opens restricted Wi-Fi panel`() {
    assertEquals(Settings.Panel.ACTION_WIFI, KioskManager.wifiSettingsIntent().action)
  }
}
