package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedMediaDao {
    @Query("SELECT * FROM downloaded_media ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadedMedia>>

    @Query("SELECT * FROM downloaded_media WHERE id = :id LIMIT 1")
    suspend fun getDownloadById(id: Int): DownloadedMedia?

    @Query("SELECT COUNT(*) FROM downloaded_media")
    fun getDownloadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(media: DownloadedMedia): Long

    @Query("DELETE FROM downloaded_media WHERE id = :id")
    suspend fun deleteDownloadById(id: Int)

    @Query("DELETE FROM downloaded_media")
    suspend fun clearAll()
}
