package com.example

import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import org.junit.Assert.assertSame
import org.junit.Test
import org.robolectric.Robolectric

class ContextActivityTest {
  @Test
  fun findsActivityInsideContextWrapper() {
    val activity = Robolectric.buildActivity(ComponentActivity::class.java).get()

    assertSame(activity, ContextWrapper(ContextWrapper(activity)).findActivity())
  }
}
