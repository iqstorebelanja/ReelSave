package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.components.AllVidTopBar
import com.example.ui.components.HugeUrlInput
import com.example.ui.components.MediaResultCard
import com.example.ui.components.SubtleGridBackground
import com.example.ui.theme.AccentError
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.GlassBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VipGold

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    activity: Activity,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refreshQualityStatuses(activity.applicationContext)
    }

    SubtleGridBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Bar: ALLVID logo (left), "20+ Platforms Supported" (center), "VIP $29/mo" switch (right)
            item {
                AllVidTopBar(
                    isVipActive = uiState.isVipActive,
                    onToggleVip = { viewModel.toggleVip(it) },
                    onVipDetailsClick = { viewModel.showVipModal(true) }
                )
            }

            // 2. Linear / Raycast Style Hero Heading
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Universal Video Downloader",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Extract 4K, 1080p, Reels, Stories & MP3 audio instantly",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // 3. Huge Centered URL Input (64px height, rounded-full, animated platform icons inside)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    HugeUrlInput(
                        value = uiState.urlInput,
                        onValueChange = { viewModel.onUrlChange(it) },
                        onPasteClick = { viewModel.pasteFromClipboard(context) },
                        onSubmit = { viewModel.fetchMedia() },
                        isLoading = uiState.isFetching
                    )
                }
            }

            // 4. Quick Sample Links (Linear styled pills, no emojis)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SampleChip(
                        icon = Icons.Default.Videocam,
                        label = "Trending Reel",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setQuickSample("https://www.instagram.com/reel/C8qLMN90abc/")
                        }
                    )
                    SampleChip(
                        icon = Icons.Default.Bolt,
                        label = "24h Story",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setQuickSample("https://www.instagram.com/stories/traveler_vibes/3345678901/")
                        }
                    )
                    SampleChip(
                        icon = Icons.Default.MusicNote,
                        label = "Music Track",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setQuickSample("https://www.instagram.com/p/C7xYzA12345/")
                        }
                    )
                }
            }

            // 5. Error Message Banner
            if (uiState.fetchError != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(1.dp, AccentError.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = uiState.fetchError,
                            color = AccentError,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 6. Success Feedback Banner
            if (uiState.downloadSuccessMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(1.dp, AccentSuccess.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccentSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = uiState.downloadSuccessMessage,
                                color = AccentSuccess,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 7. Fetched Media Result Card with QualityButton Stack
            if (uiState.fetchedMedia != null) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        MediaResultCard(
                            media = uiState.fetchedMedia,
                            isDownloading = uiState.isDownloading,
                            downloadProgress = uiState.downloadProgress,
                            activeDownloadFormat = uiState.activeDownloadFormat,
                            is1080pUnlocked = uiState.is1080pUnlocked,
                            is4kUnlocked = uiState.is4kUnlocked,
                            remaining720pToday = uiState.remaining720pToday,
                            timeRemaining1080p = uiState.timeRemaining1080p,
                            timeRemaining4k = uiState.timeRemaining4k,
                            isVipActive = uiState.isVipActive,
                            onDownload = { format ->
                                viewModel.download(activity, format)
                            }
                        )
                    }
                }
            }

            // 8. Premium Glassmorphism Feature Grid (bg-white/[0.03] border-white/[0.06])
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Engineering Architecture",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.2.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FeatureCard(
                            icon = Icons.Default.HighQuality,
                            title = "4K & 1080p Stream",
                            subtitle = "Direct lossless CDN pipeline",
                            modifier = Modifier.weight(1f)
                        )
                        FeatureCard(
                            icon = Icons.Default.Speed,
                            title = "Zero Lag Extraction",
                            subtitle = "Parallel chunk downloading",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FeatureCard(
                            icon = Icons.Default.Layers,
                            title = "Multi-Platform Parser",
                            subtitle = "Reels, Stories, TikTok & X",
                            modifier = Modifier.weight(1f)
                        )
                        FeatureCard(
                            icon = Icons.Default.Security,
                            title = "Local Vault Storage",
                            subtitle = "Privacy-first zero cloud tracking",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SampleChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(GlassBg)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonBlue,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = GlassBg),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(0.5.dp, GlassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NeonBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextTertiary,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}
