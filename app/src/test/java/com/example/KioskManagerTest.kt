package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class KioskManagerTest {

  private lateinit var context: Context

  @Before
  fun clearUpdateState() {
    context = ApplicationProvider.getApplicationContext()
    context.getSharedPreferences("nex_silent_updates", Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()
  }

  @Test
  fun `new version can be installed when no session is pending`() {
    assertTrue(KioskManager.shouldAttemptSilentInstall(context, 3))
  }

  @Test
  fun `pending version is not installed twice`() {
    setPendingUpdate(versionCode = 3, pendingSince = System.currentTimeMillis())

    assertFalse(KioskManager.shouldAttemptSilentInstall(context, 3))
    assertTrue(KioskManager.shouldAttemptSilentInstall(context, 4))
  }

  @Test
  fun `stale pending version can be retried`() {
    val moreThanTwoHoursAgo = System.currentTimeMillis() - 2 * 60 * 60 * 1000L - 1
    setPendingUpdate(versionCode = 3, pendingSince = moreThanTwoHoursAgo)

    assertTrue(KioskManager.shouldAttemptSilentInstall(context, 3))
  }

  private fun setPendingUpdate(versionCode: Int, pendingSince: Long) {
    context.getSharedPreferences("nex_silent_updates", Context.MODE_PRIVATE)
      .edit()
      .putInt("pending_version", versionCode)
      .putLong("pending_since", pendingSince)
      .commit()
  }
}
