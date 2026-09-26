package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.DownloadedMedia
import com.example.downloader.DownloadService
import com.example.ui.MainViewModel
import com.example.ui.components.SubtleGridBackground
import com.example.ui.components.VideoPlayerDialog
import com.example.ui.theme.AccentError
import com.example.ui.theme.GlassBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DownloadsScreen(
    viewModel: MainViewModel,
    downloads: List<DownloadedMedia>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") }
    var activePlaybackMedia by remember { mutableStateOf<DownloadedMedia?>(null) }
    var itemToDelete by remember { mutableStateOf<DownloadedMedia?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    val filteredList = remember(downloads, selectedFilter) {
        when (selectedFilter) {
            "VIDEOS" -> downloads.filter { it.downloadType.contains("VIDEO") }
            "AUDIO" -> downloads.filter { it.downloadType.contains("AUDIO") }
            "STORIES" -> downloads.filter { it.downloadType.contains("STORY") || it.mediaType.contains("STORY") }
            else -> downloads
        }
    }

    SubtleGridBackground(modifier = modifier) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Media Vault Title
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Media Vault",
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .border(0.5.dp, GlassBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${downloads.size} items",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Text(
                            text = "Tap to play • Long press to delete",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }

                    if (downloads.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearAllConfirm = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, GlassBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear Vault",
                                tint = AccentError,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Filters Bar
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterTabChip("ALL", "All", selectedFilter == "ALL") { selectedFilter = "ALL" }
                    FilterTabChip("VIDEOS", "Videos", selectedFilter == "VIDEOS") { selectedFilter = "VIDEOS" }
                    FilterTabChip("AUDIO", "Audio", selectedFilter == "AUDIO") { selectedFilter = "AUDIO" }
                    FilterTabChip("STORIES", "Stories", selectedFilter == "STORIES") { selectedFilter = "STORIES" }
                }
            }

            // Empty State
            if (filteredList.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 70.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .border(1.dp, GlassBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = NeonBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Vault is Empty",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Paste any video URL in the Downloader\nto save media directly to your vault.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // iOS Photos Grid Items
                items(filteredList, key = { it.id }) { item ->
                    MediaVaultGridItem(
                        item = item,
                        onPlay = { activePlaybackMedia = item },
                        onLongPress = { itemToDelete = item },
                        onShare = {
                            DownloadService.shareFile(
                                context,
                                item.localFilePath,
                                if (item.downloadType.contains("AUDIO")) "audio/*" else "video/*"
                            )
                        }
                    )
                }
            }
        }
    }

    // Video Player Dialog
    if (activePlaybackMedia != null) {
        val media = activePlaybackMedia!!
        VideoPlayerDialog(
            videoUrlOrPath = media.localFilePath,
            title = media.title,
            author = media.author,
            onDismiss = { activePlaybackMedia = null }
        )
    }

    // Long Press Delete Confirmation Dialog
    if (itemToDelete != null) {
        val item = itemToDelete!!
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text("Delete from Vault", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Delete \"${item.title}\" from your offline storage?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteItem(item.id)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentError),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { itemToDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF0F0F13)
        )
    }

    // Clear All Confirmation
    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = {
                Text("Clear Media Vault", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to remove all saved media records from ALLVID?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearAllConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentError),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Clear All", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearAllConfirm = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF0F0F13)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaVaultGridItem(
    item: DownloadedMedia,
    onPlay: () -> Unit,
    onLongPress: () -> Unit,
    onShare: () -> Unit
) {
    val formattedSize = remember(item.fileSizeBytes) {
        val mb = item.fileSizeBytes / (1024.0 * 1024.0)
        if (mb > 0.1) String.format(Locale.US, "%.1f MB", mb) else "${item.fileSizeBytes / 1024} KB"
    }

    val isAudio = item.downloadType.contains("AUDIO")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onPlay,
                onLongClick = onLongPress
            )
            .testTag("download_item_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = GlassBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Thumbnail / Background
            if (item.thumbnailUrl.isNotBlank()) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF14141A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAudio) Icons.Default.Audiotrack else Icons.Default.Videocam,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Dark vignette overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Centered Lucide-style hover Play Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, NeonBlue.copy(alpha = 0.8f), CircleShape)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAudio) Icons.Default.Audiotrack else Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = NeonBlue,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Top Left Format Tag (Raycast pill style)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = when {
                        item.downloadType.contains("AUDIO") -> "MP3"
                        item.downloadType.contains("4K") -> "4K UHD"
                        item.downloadType.contains("1080P") -> "1080p HD"
                        else -> "720p"
                    },
                    color = NeonBlue,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Top Right: Share Button
            IconButton(
                onClick = onShare,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = TextPrimary,
                    modifier = Modifier.size(13.dp)
                )
            }

            // Bottom Information Pill: Author & Size
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = "@${item.author}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedSize,
                        color = TextTertiary,
                        fontSize = 9.sp
                    )
                    Text(
                        text = "Hold to delete",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterTabChip(
    key: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) NeonBlue else Color.White.copy(alpha = 0.04f))
            .border(
                1.dp,
                if (isSelected) NeonBlue else GlassBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}
