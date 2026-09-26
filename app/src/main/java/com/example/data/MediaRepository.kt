package com.example.data

import kotlinx.coroutines.flow.Flow

class MediaRepository(private val dao: DownloadedMediaDao) {
    val allDownloads: Flow<List<DownloadedMedia>> = dao.getAllDownloads()
    val totalDownloadCount: Flow<Int> = dao.getDownloadCount()

    suspend fun insertDownload(media: DownloadedMedia): Long = dao.insertDownload(media)

    suspend fun deleteDownload(id: Int) = dao.deleteDownloadById(id)

    suspend fun clearHistory() = dao.clearAll()
}
