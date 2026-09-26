package com.example.downloader

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.ads.AdsManager
import com.example.data.DownloadedMedia
import com.example.data.MediaRepository
import com.example.network.InstagramMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

enum class DownloadFormat(val displayName: String, val tag: String) {
    VIDEO_720P("720p Standard", "720p"),
    VIDEO_1080P_HD("1080p Full HD", "1080p"),
    VIDEO_1440P_2K("1440p Quad HD", "1440p"),
    VIDEO_4K_UHD("4K Ultra HD", "4K"),
    AUDIO_MP3("Audio (MP3)", "MP3"),
    COVER_JPG("Cover (JPG)", "JPG")
}

object DownloadService {
    private const val TAG = "DownloadService"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun downloadMedia(
        context: Context,
        activity: Activity,
        media: InstagramMedia,
        format: DownloadFormat,
        repository: MediaRepository,
        onProgress: (Float) -> Unit,
        onSuccess: (DownloadedMedia) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val downloadUrl = when (format) {
                DownloadFormat.VIDEO_720P, DownloadFormat.VIDEO_1080P_HD, DownloadFormat.VIDEO_1440P_2K, DownloadFormat.VIDEO_4K_UHD ->
                    media.videoUrl ?: media.originalUrl
                DownloadFormat.AUDIO_MP3 -> media.audioUrl ?: media.videoUrl ?: media.originalUrl
                DownloadFormat.COVER_JPG -> media.thumbnailUrl
            }

            val extension = when (format) {
                DownloadFormat.VIDEO_720P, DownloadFormat.VIDEO_1080P_HD, DownloadFormat.VIDEO_1440P_2K, DownloadFormat.VIDEO_4K_UHD -> ".mp4"
                DownloadFormat.AUDIO_MP3 -> ".mp3"
                DownloadFormat.COVER_JPG -> ".jpg"
            }

            val fileName = "ReelsSave_${media.shortcode}_${format.tag}_${System.currentTimeMillis()}$extension"
            val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            val targetFile = File(targetDir, fileName)

            val request = Request.Builder().url(downloadUrl).build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful || response.body == null) {
                withContext(Dispatchers.Main) {
                    onError("Download server returned code: ${response.code}")
                }
                return@withContext
            }

            val body = response.body!!
            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(targetFile)

            val buffer = ByteArray(8 * 1024)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                downloadedBytes += bytesRead
                if (totalBytes > 0) {
                    val progress = downloadedBytes.toFloat() / totalBytes
                    withContext(Dispatchers.Main) {
                        onProgress(progress)
                    }
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            val downloadedEntity = DownloadedMedia(
                shortcode = media.shortcode,
                title = media.title,
                author = media.authorUsername,
                mediaType = media.mediaType.name,
                downloadType = format.name,
                localFilePath = targetFile.absolutePath,
                originalUrl = media.originalUrl,
                thumbnailUrl = media.thumbnailUrl,
                fileSizeBytes = targetFile.length(),
                durationSeconds = media.durationSeconds
            )

            // Save to Room DB
            val rowId = repository.insertDownload(downloadedEntity)
            val savedRecord = downloadedEntity.copy(id = rowId.toInt())

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Saved to ${targetFile.name}", Toast.LENGTH_SHORT).show()
                onSuccess(savedRecord)

                // WAJIB: AdMob Interstitial after every 2 downloads
                AdsManager.onDownloadCompleted(activity)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download error", e)
            withContext(Dispatchers.Main) {
                onError("Download failed: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    /**
     * Share downloaded file with Instagram, WhatsApp, Telegram, etc.
     */
    fun shareFile(context: Context, filePath: String, mimeType: String = "video/*") {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
                return
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share via"))
        } catch (e: Exception) {
            Log.e(TAG, "Share error", e)
            Toast.makeText(context, "Could not share file", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Open media in system video player or gallery
     */
    fun openInSystemPlayer(context: Context, filePath: String, mimeType: String = "video/*") {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
                return
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Open error", e)
            Toast.makeText(context, "No app available to play this file", Toast.LENGTH_SHORT).show()
        }
    }
}
