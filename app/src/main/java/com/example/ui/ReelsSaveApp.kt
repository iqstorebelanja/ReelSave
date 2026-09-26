package com.example.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobBannerBottom
import com.example.ads.AdMobSimulatedOverlays
import com.example.ui.components.VipMembershipDialog
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.GuideScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextTertiary

@Composable
fun ReelsSaveApp(
    viewModel: MainViewModel,
    activity: Activity
) {
    val uiState by viewModel.uiState.collectAsState()
    val downloads by viewModel.downloads.collectAsState()

    // Init AdMob in useEffect/LaunchedEffect: initAds()
    LaunchedEffect(Unit) {
        com.example.ads.AdsManager.initAds(activity)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = PureBlack,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureBlack)
            ) {
                // Linear / Raycast Style Minimal Navigation Bar
                NavigationBar(
                    containerColor = PureBlack,
                    contentColor = TextPrimary,
                    modifier = Modifier.border(1.dp, GlassBorder)
                ) {
                    NavigationBarItem(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Downloader",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Downloader",
                                fontSize = 11.sp,
                                fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NeonBlue,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary,
                            indicatorColor = NeonBlue
                        ),
                        modifier = Modifier.testTag("tab_downloader")
                    )

                    NavigationBarItem(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Media Vault",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Vault (${downloads.size})",
                                fontSize = 11.sp,
                                fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NeonBlue,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary,
                            indicatorColor = NeonBlue
                        ),
                        modifier = Modifier.testTag("tab_saved")
                    )

                    NavigationBarItem(
                        selected = uiState.selectedTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Guide",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Guide",
                                fontSize = 11.sp,
                                fontWeight = if (uiState.selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NeonBlue,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary,
                            indicatorColor = NeonBlue
                        ),
                        modifier = Modifier.testTag("tab_guide")
                    )

                    NavigationBarItem(
                        selected = uiState.selectedTab == 3,
                        onClick = { viewModel.selectTab(3) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Settings",
                                fontSize = 11.sp,
                                fontWeight = if (uiState.selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NeonBlue,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary,
                            indicatorColor = NeonBlue
                        ),
                        modifier = Modifier.testTag("tab_settings")
                    )
                }

                // AdMob Banner at bottom (hidden when VIP is active!)
                if (!uiState.isVipActive) {
                    AdMobBannerBottom(
                        modifier = Modifier.testTag("admob_bottom_banner")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.selectedTab) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    activity = activity
                )
                1 -> DownloadsScreen(
                    viewModel = viewModel,
                    downloads = downloads
                )
                2 -> GuideScreen()
                3 -> SettingsScreen(
                    viewModel = viewModel,
                    activity = activity
                )
            }
        }
    }

    // VIP Dialog
    if (uiState.showVipModal) {
        VipMembershipDialog(
            isVipActive = uiState.isVipActive,
            onToggleVip = { viewModel.toggleVip(it) },
            onDismiss = { viewModel.showVipModal(false) }
        )
    }

    // Fullscreen Ad Dialog Overlays (Simulated for emulator/offline, bypassed when VIP is active)
    if (!uiState.isVipActive) {
        AdMobSimulatedOverlays()
    }
}
