package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.AccentError
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonBlueGradient
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VipGold
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdsManager {
    private const val TAG = "AdsManager"

    // =========================================================================
    // ⚠️ REPLACE THESE TEST ADMOB IDS WITH YOUR PRODUCTION ADMOB IDS BEFORE
    // PUBLISHING TO GOOGLE PLAY STORE!
    // (Obtain production Ad Unit IDs from https://admob.google.com)
    // =========================================================================
    const val BANNER_TEST_ID = "ca-app-pub-3940256099942544/6300978111"       // <-- REPLACE WITH REAL BANNER ID
    const val INTERSTITIAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712" // <-- REPLACE WITH REAL INTERSTITIAL ID
    const val REWARDED_TEST_ID = "ca-app-pub-3940256099942544/5224354917"     // <-- REPLACE WITH REAL REWARDED ID
    const val APP_OPEN_TEST_ID = "ca-app-pub-3940256099942544/9257395921"     // <-- REPLACE WITH REAL APP OPEN ID
    // =========================================================================

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private val _downloadCountSinceLastAd = MutableStateFlow(0)
    val downloadCountSinceLastAd = _downloadCountSinceLastAd.asStateFlow()

    private val _isInterstitialLoading = MutableStateFlow(false)
    private val _isRewardedLoading = MutableStateFlow(false)

    // Simulation / Web AdSense 5s countdown modal states
    val showWebAdSenseModal = mutableStateOf(false)
    val adModalTitle = mutableStateOf("Sponsor Advertisement")
    val adModalTargetQuality = mutableStateOf("1080p")
    val adModalRequiredType = mutableStateOf("Rewarded Ad")
    val adModalSecondsRemaining = mutableIntStateOf(5)
    val isAdCountDownActive = mutableStateOf(false)
    private var onAdRewardEarned: (() -> Unit)? = null

    // Fallback in-app simulation states
    val showSimulatedInterstitial = mutableStateOf(false)

    /**
     * Alias for initAds to match ads.ts requirement:
     * "Init AdMob in useEffect: initAds()"
     */
    fun initAds(context: Context) {
        initialize(context)
    }

    fun initialize(context: Context) {
        try {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "AdMob initialized: $initializationStatus")
                loadInterstitialAd(context)
                loadRewardedAd(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AdMob", e)
        }
    }

    fun loadInterstitialAd(context: Context) {
        if (interstitialAd != null || _isInterstitialLoading.value) return
        _isInterstitialLoading.value = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_TEST_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    _isInterstitialLoading.value = false
                    Log.d(TAG, "Interstitial Ad Loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    _isInterstitialLoading.value = false
                    Log.w(TAG, "Interstitial Ad failed: ${error.message}")
                }
            }
        )
    }

    fun loadRewardedAd(context: Context) {
        if (rewardedAd != null || _isRewardedLoading.value) return
        _isRewardedLoading.value = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_TEST_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    _isRewardedLoading.value = false
                    Log.d(TAG, "Rewarded Ad Loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    _isRewardedLoading.value = false
                    Log.w(TAG, "Rewarded Ad failed: ${error.message}")
                }
            }
        )
    }

    /**
     * Unlock Quality Gateway (replaces all existing ad logic):
     *
     * Rules:
     * - 720p: FREE (up to 3x per day). If daily limit reached, watch 1 ad to unlock 24h unlimited.
     * - 1080p: 1 Rewarded Ad (unlocks 24 hours).
     * - 1440p / 4K: Interstitial + Rewarded (unlocks 24 hours).
     * - VIP: Bypasses all ads immediately.
     */
    fun unlockQuality(
        quality: String,
        activity: Activity,
        isVipActive: Boolean = false,
        onUnlocked: () -> Unit
    ) {
        val q = quality.lowercase()
        val context = activity.applicationContext

        // 1. VIP bypass
        if (isVipActive) {
            onUnlocked()
            return
        }

        // 2. Audio & Cover are always free
        if (q == "mp3" || q == "jpg") {
            onUnlocked()
            return
        }

        // 3. Check if already unlocked within 24 hours
        if (QualityStorage.isQualityUnlocked(context, q)) {
            onUnlocked()
            return
        }

        // 4. Handle 720p free tier (3x per day)
        if (q == "720p") {
            val remainingToday = QualityStorage.getRemaining720pToday(context)
            if (remainingToday > 0) {
                QualityStorage.consume720pDownload(context)
                onUnlocked()
                return
            } else {
                // Daily limit reached -> Watch 1 sponsor ad for 24h unlimited 720p
                showAdForQuality(
                    activity = activity,
                    quality = "720p",
                    title = "720p Free Limit Reached (3/3 Used)",
                    requiredType = "1 Rewarded Ad (24h Pass)",
                    onReward = {
                        QualityStorage.saveQualityUnlocked(context, "720p")
                        onUnlocked()
                    }
                )
                return
            }
        }

        // 5. Handle 1080p (1 Rewarded Ad)
        if (q == "1080p") {
            showAdForQuality(
                activity = activity,
                quality = "1080p",
                title = "Unlock 1080p Full HD",
                requiredType = "1 Rewarded Ad (24h Access)",
                onReward = {
                    QualityStorage.saveQualityUnlocked(context, "1080p")
                    onUnlocked()
                }
            )
            return
        }

        // 6. Handle 1440p / 4K (Interstitial + Rewarded)
        if (q == "1440p" || q == "4k") {
            // First show Interstitial, then Rewarded
            showInterstitialThenRewarded(
                activity = activity,
                quality = q,
                onComplete = {
                    QualityStorage.saveQualityUnlocked(context, "1440p")
                    QualityStorage.saveQualityUnlocked(context, "4k")
                    onUnlocked()
                }
            )
            return
        }

        // Default fallback
        onUnlocked()
    }

    /**
     * Combined Interstitial + Rewarded sequence for 1440p / 4K
     */
    private fun showInterstitialThenRewarded(
        activity: Activity,
        quality: String,
        onComplete: () -> Unit
    ) {
        val context = activity.applicationContext
        val ad = interstitialAd

        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd(context)
                    // Interstitial finished -> now trigger Rewarded Ad
                    showAdForQuality(
                        activity = activity,
                        quality = quality,
                        title = "Step 2/2: Unlock 4K Ultra HD",
                        requiredType = "Rewarded Ad",
                        onReward = onComplete
                    )
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    loadInterstitialAd(context)
                    showAdForQuality(
                        activity = activity,
                        quality = quality,
                        title = "Unlock 4K Ultra HD",
                        requiredType = "Rewarded Ad",
                        onReward = onComplete
                    )
                }
            }
            ad.show(activity)
        } else {
            // Web / Preview fallback: launch 5s countdown modal
            showAdForQuality(
                activity = activity,
                quality = quality,
                title = "Unlock 4K Ultra HD (Interstitial + Rewarded)",
                requiredType = "Interstitial + Rewarded Ad (5s)",
                onReward = onComplete
            )
        }
    }

    /**
     * Show Rewarded Ad with fallback to 5s Web/AdSense countdown modal
     */
    private fun showAdForQuality(
        activity: Activity,
        quality: String,
        title: String,
        requiredType: String,
        onReward: () -> Unit
    ) {
        val ad = rewardedAd
        val context = activity.applicationContext

        if (ad != null) {
            var rewardDelivered = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd(context)
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    rewardedAd = null
                    loadRewardedAd(context)
                    // Fallback to web 5s modal if ad fails on device
                    triggerWebCountdownModal(quality, title, requiredType, onReward)
                }
            }
            ad.show(activity) { rewardItem ->
                if (!rewardDelivered) {
                    rewardDelivered = true
                    onReward()
                }
            }
        } else {
            // Web version / Preview mode: 5s countdown modal with Google AdSense slot
            triggerWebCountdownModal(quality, title, requiredType, onReward)
            loadRewardedAd(context)
        }
    }

    private fun triggerWebCountdownModal(
        quality: String,
        title: String,
        requiredType: String,
        onReward: () -> Unit
    ) {
        adModalTargetQuality.value = quality
        adModalTitle.value = title
        adModalRequiredType.value = requiredType
        adModalSecondsRemaining.intValue = 5
        isAdCountDownActive.value = true
        onAdRewardEarned = onReward
        showWebAdSenseModal.value = true
    }

    fun completeWebReward() {
        showWebAdSenseModal.value = false
        isAdCountDownActive.value = false
        onAdRewardEarned?.invoke()
        onAdRewardEarned = null
    }

    fun cancelWebModal() {
        showWebAdSenseModal.value = false
        isAdCountDownActive.value = false
        onAdRewardEarned = null
    }

    /**
     * Triggered every 2 downloads for general app maintenance
     */
    fun onDownloadCompleted(activity: Activity) {
        val nextCount = _downloadCountSinceLastAd.value + 1
        _downloadCountSinceLastAd.value = nextCount

        if (nextCount >= 2) {
            _downloadCountSinceLastAd.value = 0
            showInterstitialAd(activity)
        }
    }

    private fun showInterstitialAd(activity: Activity) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd(activity.applicationContext)
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    loadInterstitialAd(activity.applicationContext)
                }
            }
            ad.show(activity)
        } else {
            showSimulatedInterstitial.value = true
            loadInterstitialAd(activity.applicationContext)
        }
    }

    fun showRewardedAd(
        activity: Activity,
        title: String,
        description: String,
        onRewardUnlocked: () -> Unit
    ) {
        showAdForQuality(
            activity = activity,
            quality = "custom",
            title = title,
            requiredType = "Rewarded Ad",
            onReward = onRewardUnlocked
        )
    }
}

/**
 * Bottom Banner Ad Component
 */
@Composable
fun AdMobBannerBottom(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(DarkSurface)
            .border(1.dp, DarkCardBorder),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdsManager.BANNER_TEST_ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

/**
 * Web Version 5s Countdown Modal with AdSense Placeholder Div
 * Simulates rewarded ad verification in Web & Preview environments
 */
@Composable
fun AdSenseCountdownModal() {
    if (AdsManager.showWebAdSenseModal.value) {
        var secondsLeft by remember { mutableIntStateOf(5) }
        var isCompleted by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            secondsLeft = 5
            isCompleted = false
            while (secondsLeft > 0) {
                kotlinx.coroutines.delay(1000L)
                secondsLeft -= 1
            }
            isCompleted = true
        }

        AlertDialog(
            onDismissRequest = {
                if (isCompleted) {
                    AdsManager.cancelWebModal()
                }
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFF4B400))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Ad",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Google AdSense",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    if (isCompleted) {
                        IconButton(
                            onClick = { AdsManager.cancelWebModal() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = AdsManager.adModalTitle.value,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Realistic Google AdSense Placeholder Slot (ins.adsbygoogle replica)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = NeonBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "adsbygoogle • Responsive Display Unit",
                                    color = TextTertiary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "High-Speed CDN Engine Sponsor",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Lossless 4K & 1080p multi-stream parallel extraction pipeline.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 5s Countdown Display
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (!isCompleted) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = NeonBlue,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Ad playing: ${secondsLeft}s remaining",
                                        color = NeonBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AccentSuccess,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Reward verified! 24h access ready",
                                        color = AccentSuccess,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (!isCompleted) "Please wait for the 5-second sponsor countdown to complete." else "Quality unlocked for the next 24 hours.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        AdsManager.completeWebReward()
                    },
                    enabled = isCompleted,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) NeonBlue else Color.White.copy(alpha = 0.1f),
                        disabledContainerColor = Color.White.copy(alpha = 0.05f)
                    )
                ) {
                    Text(
                        text = if (isCompleted) "Claim 24h Unlock & Download" else "Wait (${secondsLeft}s)",
                        color = if (isCompleted) Color.Black else TextTertiary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { AdsManager.cancelWebModal() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Text("Cancel", color = TextSecondary, fontSize = 12.sp)
                }
            },
            containerColor = Color(0xFF0B0F19),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

/**
 * Simulated Interstitial and Rewarded Ad Dialogs
 */
@Composable
fun AdMobSimulatedOverlays() {
    AdSenseCountdownModal()

    if (AdsManager.showSimulatedInterstitial.value) {
        AlertDialog(
            onDismissRequest = { AdsManager.showSimulatedInterstitial.value = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = NeonBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AdMob Interstitial Ad (Test)",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Triggered automatically after 2 downloads.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF101426))
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ALLVID High-Speed CDN",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Lossless multi-platform extraction pipeline.",
                                color = TextTertiary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { AdsManager.showSimulatedInterstitial.value = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
                ) {
                    Text("Close Ad", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF080C14)
        )
    }
}
