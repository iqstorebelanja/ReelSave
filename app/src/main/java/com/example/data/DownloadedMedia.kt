package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_media")
data class DownloadedMedia(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val shortcode: String,
    val title: String,
    val author: String,
    val mediaType: String, // REEL, STORY, POST
    val downloadType: String, // VIDEO_HD, AUDIO_MP3, COVER_JPG, STORY_HD
    val localFilePath: String,
    val originalUrl: String,
    val thumbnailUrl: String,
    val fileSizeBytes: Long = 0L,
    val durationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
