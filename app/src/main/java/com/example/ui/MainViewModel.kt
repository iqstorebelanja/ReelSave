package com.example.ui

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdsManager
import com.example.ads.QualityStorage
import com.example.data.DownloadedMedia
import com.example.data.MediaRepository
import com.example.downloader.DownloadFormat
import com.example.downloader.DownloadService
import com.example.network.InstagramExtractor
import com.example.network.InstagramMedia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiState(
    val urlInput: String = "",
    val isFetching: Boolean = false,
    val fetchError: String? = null,
    val fetchedMedia: InstagramMedia? = null,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val activeDownloadFormat: DownloadFormat? = null,
    val downloadSuccessMessage: String? = null,
    val selectedTab: Int = 0, // 0: Downloader, 1: Media Vault, 2: Platforms, 3: Settings
    // Quality Gate States
    val is1080pUnlocked: Boolean = false,
    val is1440pUnlocked: Boolean = false,
    val is4kUnlocked: Boolean = false,
    val remaining720pToday: Int = 3,
    val timeRemaining1080p: String? = null,
    val timeRemaining4k: String? = null,
    val adsWatchedFor1440p: Int = 0, // Kept for backwards compatibility
    val isVipActive: Boolean = false, // VIP $29/mo Mode
    val showVipModal: Boolean = false
)

class MainViewModel(private val repository: MediaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val downloads: StateFlow<List<DownloadedMedia>> = repository.allDownloads
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalDownloadsCount: StateFlow<Int> = repository.totalDownloadCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun onUrlChange(newUrl: String) {
        _uiState.value = _uiState.value.copy(
            urlInput = newUrl,
            fetchError = null
        )
    }

    fun pasteFromClipboard(context: Context) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString() ?: ""
                if (text.isNotBlank()) {
                    _uiState.value = _uiState.value.copy(
                        urlInput = text.trim(),
                        fetchError = null
                    )
                    // Auto-fetch if valid IG link
                    if (InstagramExtractor.isValidInstagramUrl(text)) {
                        fetchMedia()
                    }
                }
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(fetchError = "Could not paste from clipboard")
        }
    }

    fun clearInput() {
        _uiState.value = _uiState.value.copy(
            urlInput = "",
            fetchError = null
        )
    }

    fun selectTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun toggleVip(active: Boolean) {
        _uiState.value = _uiState.value.copy(
            isVipActive = active,
            downloadSuccessMessage = if (active) "VIP Active: Ad-free downloads & instant 4K access" else "VIP Deactivated"
        )
    }

    fun showVipModal(show: Boolean) {
        _uiState.value = _uiState.value.copy(showVipModal = show)
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(
            downloadSuccessMessage = null,
            fetchError = null
        )
    }

    fun setQuickSample(sampleUrl: String) {
        _uiState.value = _uiState.value.copy(
            urlInput = sampleUrl,
            fetchError = null
        )
        fetchMedia()
    }

    fun fetchMedia() {
        val url = _uiState.value.urlInput.trim()
        if (url.isBlank()) {
            _uiState.value = _uiState.value.copy(fetchError = "Please enter or paste a valid video URL.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isFetching = true,
                fetchError = null
            )

            val result = InstagramExtractor.extract(url)
            result.onSuccess { media ->
                _uiState.value = _uiState.value.copy(
                    isFetching = false,
                    fetchedMedia = media,
                    fetchError = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isFetching = false,
                    fetchError = error.localizedMessage ?: "Failed to extract media from URL."
                )
            }
        }
    }

    fun refreshQualityStatuses(context: Context) {
        val remaining720p = QualityStorage.getRemaining720pToday(context)
        val unlocked1080p = QualityStorage.isQualityUnlocked(context, "1080p")
        val unlocked1440p = QualityStorage.isQualityUnlocked(context, "1440p") || QualityStorage.isQualityUnlocked(context, "4k")
        val time1080p = QualityStorage.getRemainingUnlockTimeFormatted(context, "1080p")
        val time4k = QualityStorage.getRemainingUnlockTimeFormatted(context, "4k")

        _uiState.value = _uiState.value.copy(
            remaining720pToday = remaining720p,
            is1080pUnlocked = unlocked1080p,
            is1440pUnlocked = unlocked1440p,
            is4kUnlocked = unlocked1440p,
            timeRemaining1080p = time1080p,
            timeRemaining4k = time4k
        )
    }

    /**
     * Replaced all existing ad logic with AdsManager.unlockQuality():
     * - 720p: FREE (up to 3x/day limit, then 1 ad for 24h pass)
     * - 1080p: 1 Rewarded Ad (24h expiry)
     * - 1440p / 4K: Interstitial + Rewarded Ad (24h expiry)
     * - VIP: Bypasses all ads
     */
    fun download(activity: Activity, format: DownloadFormat) {
        val media = _uiState.value.fetchedMedia ?: return
        if (_uiState.value.isDownloading) return

        val qualityKey = when (format) {
            DownloadFormat.VIDEO_720P -> "720p"
            DownloadFormat.VIDEO_1080P_HD -> "1080p"
            DownloadFormat.VIDEO_1440P_2K -> "1440p"
            DownloadFormat.VIDEO_4K_UHD -> "4k"
            DownloadFormat.AUDIO_MP3 -> "mp3"
            DownloadFormat.COVER_JPG -> "jpg"
        }

        AdsManager.unlockQuality(
            quality = qualityKey,
            activity = activity,
            isVipActive = _uiState.value.isVipActive,
            onUnlocked = {
                refreshQualityStatuses(activity.applicationContext)
                executeDownload(activity, media, format)
            }
        )
    }

    private fun executeDownload(activity: Activity, media: InstagramMedia, format: DownloadFormat) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDownloading = true,
                downloadProgress = 0f,
                activeDownloadFormat = format,
                downloadSuccessMessage = null
            )

            DownloadService.downloadMedia(
                context = activity.applicationContext,
                activity = activity,
                media = media,
                format = format,
                repository = repository,
                onProgress = { progress ->
                    _uiState.value = _uiState.value.copy(downloadProgress = progress)
                },
                onSuccess = { record ->
                    _uiState.value = _uiState.value.copy(
                        isDownloading = false,
                        downloadProgress = 1f,
                        activeDownloadFormat = null,
                        downloadSuccessMessage = "Downloaded: ${record.title}"
                    )
                },
                onError = { error ->
                    _uiState.value = _uiState.value.copy(
                        isDownloading = false,
                        activeDownloadFormat = null,
                        fetchError = error
                    )
                }
            )
        }
    }

    fun deleteItem(id: Int) {
        viewModelScope.launch {
            repository.deleteDownload(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
