package com.example.ads

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages persistent storage (Android equivalent of localStorage):
 * - 24-hour expiry for unlocked qualities (1080p, 1440p, 4K)
 * - 3x per day free limit for 720p downloads
 */
object QualityStorage {
    private const val PREFS_NAME = "allvid_quality_storage"
    private const val KEY_UNLOCK_PREFIX = "unlocked_time_"
    private const val KEY_FREE_720P_DATE = "free_720p_date"
    private const val KEY_FREE_720P_COUNT = "free_720p_count"

    const val MAX_FREE_720P_PER_DAY = 3
    const val EXPIRY_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
    }

    /**
     * Checks if a quality is currently unlocked (within 24h)
     */
    fun isQualityUnlocked(context: Context, quality: String): Boolean {
        val q = quality.lowercase()
        if (q == "mp3" || q == "jpg") return true

        val prefs = getPrefs(context)
        val unlockTime = prefs.getLong(KEY_UNLOCK_PREFIX + q, 0L)
        if (unlockTime == 0L) return false

        val now = System.currentTimeMillis()
        val isStillValid = (now - unlockTime) < EXPIRY_DURATION_MS
        if (!isStillValid) {
            // Expired -> clean up
            prefs.edit().remove(KEY_UNLOCK_PREFIX + q).apply()
            return false
        }
        return true
    }

    /**
     * Returns remaining hours/minutes for an unlocked quality, or null if locked
     */
    fun getRemainingUnlockTimeFormatted(context: Context, quality: String): String? {
        val prefs = getPrefs(context)
        val unlockTime = prefs.getLong(KEY_UNLOCK_PREFIX + quality.lowercase(), 0L)
        if (unlockTime == 0L) return null

        val now = System.currentTimeMillis()
        val remainingMs = EXPIRY_DURATION_MS - (now - unlockTime)
        if (remainingMs <= 0) {
            prefs.edit().remove(KEY_UNLOCK_PREFIX + quality.lowercase()).apply()
            return null
        }

        val hours = (remainingMs / (1000 * 60 * 60)).toInt()
        val minutes = ((remainingMs / (1000 * 60)) % 60).toInt()
        return if (hours > 0) "${hours}h ${minutes}m left" else "${minutes}m left"
    }

    /**
     * Mark quality as unlocked for 24 hours
     */
    fun saveQualityUnlocked(context: Context, quality: String) {
        val prefs = getPrefs(context)
        prefs.edit().putLong(KEY_UNLOCK_PREFIX + quality.lowercase(), System.currentTimeMillis()).apply()
    }

    /**
     * Get remaining 720p free downloads for today (max 3)
     */
    fun getRemaining720pToday(context: Context): Int {
        val prefs = getPrefs(context)
        val savedDate = prefs.getString(KEY_FREE_720P_DATE, "") ?: ""
        val today = getTodayDateString()
        if (savedDate != today) {
            return MAX_FREE_720P_PER_DAY
        }
        val count = prefs.getInt(KEY_FREE_720P_COUNT, 0)
        return (MAX_FREE_720P_PER_DAY - count).coerceAtLeast(0)
    }

    /**
     * Consume one 720p free download for today.
     * Returns true if successfully consumed, false if daily limit reached.
     */
    fun consume720pDownload(context: Context): Boolean {
        val prefs = getPrefs(context)
        val savedDate = prefs.getString(KEY_FREE_720P_DATE, "") ?: ""
        val today = getTodayDateString()
        val editor = prefs.edit()

        var currentCount = 0
        if (savedDate == today) {
            currentCount = prefs.getInt(KEY_FREE_720P_COUNT, 0)
        } else {
            editor.putString(KEY_FREE_720P_DATE, today)
        }

        if (currentCount >= MAX_FREE_720P_PER_DAY) {
            return false
        }

        editor.putInt(KEY_FREE_720P_COUNT, currentCount + 1).apply()
        return true
    }

    /**
     * Reset all unlocks and daily limits (useful for testing & diagnostics)
     */
    fun resetAll(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
