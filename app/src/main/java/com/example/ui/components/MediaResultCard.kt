package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.downloader.DownloadFormat
import com.example.network.InstagramMedia
import com.example.network.MediaType
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.GlassBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderGlow
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonBlueGradient
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VipGold

@Composable
fun MediaResultCard(
    media: InstagramMedia,
    isDownloading: Boolean,
    downloadProgress: Float,
    activeDownloadFormat: DownloadFormat?,
    is1080pUnlocked: Boolean,
    adsWatchedFor1440p: Int = 0,
    is4kUnlocked: Boolean = false,
    remaining720pToday: Int = 3,
    timeRemaining1080p: String? = null,
    timeRemaining4k: String? = null,
    isVipActive: Boolean = false,
    onDownload: (DownloadFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPlayerDialog by remember { mutableStateOf(false) }
    var isCaptionExpanded by remember { mutableStateOf(false) }

    // Segmented Quality Selector State: 720p, 1080p, 4K
    var selectedFormat by remember { mutableStateOf(DownloadFormat.VIDEO_1080P_HD) }

    // Glassmorphism Card: bg-white/[0.03] border-white/[0.06]
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(containerColor = GlassBg),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Author / Creator & Platform Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with sleek neon ring
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, NeonBlue.copy(alpha = 0.6f), CircleShape)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = media.authorAvatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = media.authorUsername,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = NeonBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = media.authorFullName.ifBlank { "Original Creator" },
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                // Platform / Media Type Tag (Raycast pill style)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (media.isStory) "STORY" else media.mediaType.name,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Video / Image Preview with Hover Play Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .clickable {
                        if (!media.videoUrl.isNullOrEmpty()) {
                            showPlayerDialog = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = media.thumbnailUrl,
                    contentDescription = "Cover Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Sleek dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        )
                )

                // Lucide-style centered Play button
                if (!media.videoUrl.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .border(1.5.dp, NeonBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Preview",
                            tint = NeonBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Bottom Left: Stats Pill
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = media.likeCount,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Top Right: VIP / Source Pill
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .border(
                            0.5.dp,
                            if (isVipActive) VipGold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isVipActive) "VIP DIRECT" else "ORIGINAL STREAM",
                        color = if (isVipActive) VipGold else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Caption Section
            if (media.caption.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = media.caption,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        maxLines = if (isCaptionExpanded) 10 else 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isCaptionExpanded = !isCaptionExpanded }
                    )

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Caption", media.caption)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Caption copied", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Caption",
                            tint = TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Download Progress Indicator
            AnimatedVisibility(visible = isDownloading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Downloading ${activeDownloadFormat?.displayName ?: "Media"}...",
                            color = NeonBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(downloadProgress * 100).toInt()}%",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = NeonBlue,
                        trackColor = Color.White.copy(alpha = 0.08f)
                    )
                }
            }

            // Quality Selector Header (Linear / Raycast style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = NeonBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Download Qualities",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                if (isVipActive) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = null,
                            tint = VipGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "VIP Bypass Active",
                            color = VipGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // QualityButton Stack for all download options
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. 720p Standard (FREE - 3x per day limit)
                QualityButton(
                    qualityTitle = "720p Standard",
                    subtitle = "HD 1280x720 • Instant Fast Stream",
                    badgeText = when {
                        isVipActive -> "VIP FREE"
                        remaining720pToday > 0 -> "$remaining720pToday/3 FREE TODAY"
                        else -> "DAILY LIMIT (WATCH AD)"
                    },
                    isUnlocked = isVipActive || remaining720pToday > 0,
                    isDownloading = isDownloading && activeDownloadFormat == DownloadFormat.VIDEO_720P,
                    isVipActive = isVipActive,
                    onClick = { onDownload(DownloadFormat.VIDEO_720P) }
                )

                // 2. 1080p Full HD (1 Rewarded Ad -> 24h Unlock)
                QualityButton(
                    qualityTitle = "1080p Full HD",
                    subtitle = "Crisp 1920x1080 • 60fps High Bitrate",
                    badgeText = when {
                        isVipActive -> "VIP UNLOCKED"
                        is1080pUnlocked -> timeRemaining1080p ?: "24H UNLOCKED"
                        else -> "1 REWARDED AD"
                    },
                    isUnlocked = isVipActive || is1080pUnlocked,
                    isDownloading = isDownloading && activeDownloadFormat == DownloadFormat.VIDEO_1080P_HD,
                    isVipActive = isVipActive,
                    onClick = { onDownload(DownloadFormat.VIDEO_1080P_HD) }
                )

                // 3. 4K Ultra HD (Interstitial + Rewarded -> 24h Unlock)
                QualityButton(
                    qualityTitle = "4K Ultra HD",
                    subtitle = "Cinema 3840x2160 • Original Lossless Master",
                    badgeText = when {
                        isVipActive -> "VIP UNLOCKED"
                        is4kUnlocked -> timeRemaining4k ?: "24H UNLOCKED"
                        else -> "INTERSTITIAL + REWARDED"
                    },
                    isUnlocked = isVipActive || is4kUnlocked,
                    isDownloading = isDownloading && (activeDownloadFormat == DownloadFormat.VIDEO_1440P_2K || activeDownloadFormat == DownloadFormat.VIDEO_4K_UHD),
                    isVipActive = isVipActive,
                    onClick = { onDownload(DownloadFormat.VIDEO_4K_UHD) }
                )

                // 4. Secondary Row: Audio (MP3) and Cover (JPG)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QualityButton(
                        qualityTitle = "Audio MP3",
                        subtitle = "320kbps Audio",
                        badgeText = "FREE",
                        isUnlocked = true,
                        isDownloading = isDownloading && activeDownloadFormat == DownloadFormat.AUDIO_MP3,
                        isVipActive = isVipActive,
                        icon = Icons.Default.Audiotrack,
                        onClick = { onDownload(DownloadFormat.AUDIO_MP3) },
                        modifier = Modifier.weight(1f)
                    )

                    QualityButton(
                        qualityTitle = "Cover JPG",
                        subtitle = "Original Poster",
                        badgeText = "FREE",
                        isUnlocked = true,
                        isDownloading = isDownloading && activeDownloadFormat == DownloadFormat.COVER_JPG,
                        isVipActive = isVipActive,
                        icon = Icons.Default.Image,
                        onClick = { onDownload(DownloadFormat.COVER_JPG) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Video Player Modal
    if (showPlayerDialog && !media.videoUrl.isNullOrEmpty()) {
        VideoPlayerDialog(
            videoUrlOrPath = media.videoUrl,
            title = media.title,
            author = media.authorUsername,
            onDismiss = { showPlayerDialog = false }
        )
    }
}

/**
 * YouTube / Linear style quality segment
 * Active state: Neon blue #00D1FF
 */
@Composable
private fun QualitySegment(
    title: String,
    badge: String,
    isLocked: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) NeonBlue else Color.Transparent,
        label = "segment_bg"
    )

    val animatedBorder by animateColorAsState(
        targetValue = if (isSelected) NeonBlue else Color.Transparent,
        label = "segment_border"
    )

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(animatedBg)
            .border(1.dp, animatedBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
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
                if (isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = if (isSelected) Color.Black else TextTertiary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                    text = title,
                    color = if (isSelected) Color.Black else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = badge,
                color = if (isSelected) Color.Black.copy(alpha = 0.8f) else if (isLocked) TextTertiary else AccentSuccess,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
