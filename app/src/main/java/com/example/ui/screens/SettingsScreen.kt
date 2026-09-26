package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdsManager
import com.example.ui.MainViewModel
import com.example.ui.components.SubtleGridBackground
import com.example.ui.theme.AccentError
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.GlassBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VipGold

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    activity: Activity,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val downloadCountByAds by AdsManager.downloadCountSinceLastAd.collectAsState()
    val totalDownloads by viewModel.totalDownloadsCount.collectAsState()

    SubtleGridBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Settings & Diagnostics",
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "AdMob telemetry, VIP membership and app configuration",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // VIP Membership Pass Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (uiState.isVipActive) VipGold.copy(alpha = 0.5f) else GlassBorder,
                            RoundedCornerShape(18.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = GlassBg),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                    .background(VipGold.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Diamond,
                                        contentDescription = null,
                                        tint = VipGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "VIP Membership",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = if (uiState.isVipActive) "Active ($29/mo tier)" else "Standard Free Plan",
                                        color = if (uiState.isVipActive) VipGold else TextTertiary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Switch(
                                checked = uiState.isVipActive,
                                onCheckedChange = { viewModel.toggleVip(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = VipGold,
                                    uncheckedThumbColor = TextTertiary,
                                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                                )
                            )
                        }

                        Text(
                            text = "VIP members get zero ads across the entire app and instant 4K Ultra HD downloads without watching sponsor videos.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // AdMob Configuration Panel
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorder, RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = GlassBg),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdsClick,
                                    contentDescription = null,
                                    tint = NeonBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AdMob System Integration",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (uiState.isVipActive) "Paused (VIP active)" else "Operational (Test ID Pipeline)",
                                    color = if (uiState.isVipActive) VipGold else AccentSuccess,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        AdRowItem(
                            title = "Bottom Banner Ad",
                            unitId = AdsManager.BANNER_TEST_ID,
                            status = if (uiState.isVipActive) "Hidden for VIP" else "Active at screen base",
                            badgeColor = NeonBlue
                        )

                        AdRowItem(
                            title = "Interstitial Ad (1 per 2 Downloads)",
                            unitId = AdsManager.INTERSTITIAL_TEST_ID,
                            status = "Counter: $downloadCountByAds / 2 downloads completed",
                            badgeColor = NeonBlue
                        )

                        AdRowItem(
                            title = "Rewarded Ad (Quality Gate)",
                            unitId = AdsManager.REWARDED_TEST_ID,
                            status = "720p = Free | 1080p = 1 Ad | 4K = 2 Ads",
                            badgeColor = NeonBlue
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    AdsManager.loadInterstitialAd(activity)
                                    AdsManager.showSimulatedInterstitial.value = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                            ) {
                                Text("Test Interstitial", fontSize = 11.sp, color = TextPrimary)
                            }

                            OutlinedButton(
                                onClick = {
                                    AdsManager.showRewardedAd(
                                        activity = activity,
                                        title = "Test Rewarded Ad",
                                        description = "Testing AdMob Rewarded Video for Quality Gate."
                                    ) {
                                        Toast.makeText(context, "Quality Gate Reward Granted", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                            ) {
                                Text("Test Rewarded", fontSize = 11.sp, color = NeonBlue)
                            }
                        }
                    }
                }
            }

            // Storage & Vault Info
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorder, RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = GlassBg),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = NeonBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Storage & Vault",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        AppInfoRow(label = "Vault Item Count", value = "$totalDownloads items")
                        AppInfoRow(label = "Directory Path", value = "Download/ALLVID")
                        AppInfoRow(label = "Private Storage Mode", value = "Enabled")
                    }
                }
            }

            // About Application
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorder, RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = GlassBg),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = NeonBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "System Information",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        AppInfoRow(label = "Application", value = "ALLVID Pro")
                        AppInfoRow(label = "Version", value = "2.0.0 (Linear Edition)")
                        AppInfoRow(label = "Engine", value = "Lossless Multi-Stream CDN")
                        AppInfoRow(label = "Package ID", value = "com.reelssave.downloader")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}

@Composable
private fun AdRowItem(
    title: String,
    unitId: String,
    status: String,
    badgeColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(0.5.dp, GlassBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "TEST ID",
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "ID: $unitId",
                color = TextTertiary,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = status,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AppInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Text(text = value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}
