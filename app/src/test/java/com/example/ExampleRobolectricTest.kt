package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ads.AdsManager
import com.example.network.InstagramExtractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ALLVID", appName)
  }

  @Test
  fun `verify instagram url detection`() {
    assertTrue(InstagramExtractor.isValidInstagramUrl("https://www.instagram.com/reel/C8qLMN90abc/"))
    assertTrue(InstagramExtractor.isValidInstagramUrl("https://instagram.com/p/C7xYzA12345/"))
    assertTrue(InstagramExtractor.isValidInstagramUrl("https://www.instagram.com/stories/creator/123/"))
  }

  @Test
  fun `verify admob test ids configured`() {
    assertEquals("ca-app-pub-3940256099942544/6300978111", AdsManager.BANNER_TEST_ID)
    assertEquals("ca-app-pub-3940256099942544/1033173712", AdsManager.INTERSTITIAL_TEST_ID)
    assertEquals("ca-app-pub-3940256099942544/5224354917", AdsManager.REWARDED_TEST_ID)
  }
}
