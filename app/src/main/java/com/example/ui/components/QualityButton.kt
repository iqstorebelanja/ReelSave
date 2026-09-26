package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.AccentWarning
import com.example.ui.theme.GlassBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderGlow
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonBlueGradient
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VipGold

/**
 * QualityButton Component
 *
 * Dedicated Linear/Raycast-styled quality selection and download action button.
 * Used for all download qualities: 720p, 1080p, 1440p, 4K, Audio MP3, and Cover JPG.
 *
 * Visual Rules:
 * - 720p: FREE badge (or remaining 3x/day counter: "2 left today")
 * - 1080p: 1 Rewarded Ad (or "24h Unlocked")
 * - 1440p / 4K: Interstitial + Rewarded (or "24h Unlocked")
 * - VIP: Gold diamond badge & instant download
 */
@Composable
fun QualityButton(
    qualityTitle: String,
    subtitle: String,
    badgeText: String,
    isUnlocked: Boolean,
    isSelected: Boolean = false,
    isDownloading: Boolean = false,
    isVipActive: Boolean = false,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> NeonBlue
            isUnlocked && isVipActive -> VipGold.copy(alpha = 0.6f)
            isUnlocked -> NeonBlue.copy(alpha = 0.4f)
            else -> GlassBorder
        },
        label = "quality_border_color"
    )

    val backgroundColor = when {
        isSelected -> NeonBlue.copy(alpha = 0.08f)
        isUnlocked && isVipActive -> VipGold.copy(alpha = 0.04f)
        isUnlocked -> Color.White.copy(alpha = 0.04f)
        else -> GlassBg
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !isDownloading, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("quality_button_${qualityTitle.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Column: Icon + Quality Title + Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Quality indicator icon or format glyph
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                isVipActive -> VipGold.copy(alpha = 0.15f)
                                isUnlocked -> NeonBlue.copy(alpha = 0.15f)
                                else -> Color.White.copy(alpha = 0.06f)
                            }
                        )
                        .border(
                            0.5.dp,
                            when {
                                isVipActive -> VipGold.copy(alpha = 0.3f)
                                isUnlocked -> NeonBlue.copy(alpha = 0.3f)
                                else -> GlassBorder
                            },
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = NeonBlue,
                            strokeWidth = 2.dp
                        )
                    } else if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = when {
                                isVipActive -> VipGold
                                isUnlocked -> NeonBlue
                                else -> TextSecondary
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    } else if (isVipActive) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = null,
                            tint = VipGold,
                            modifier = Modifier.size(18.dp)
                        )
                    } else if (isUnlocked) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = NeonBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = qualityTitle,
                            color = if (isSelected) NeonBlue else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (isVipActive) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VIP",
                                color = VipGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(VipGold.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Column: Badge (Requirement / Expiry / Action)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                // Pill Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isVipActive -> VipGold.copy(alpha = 0.15f)
                                isUnlocked -> AccentSuccess.copy(alpha = 0.12f)
                                badgeText.contains("LIMIT", ignoreCase = true) -> AccentWarning.copy(alpha = 0.15f)
                                else -> NeonBlue.copy(alpha = 0.12f)
                            }
                        )
                        .border(
                            0.5.dp,
                            when {
                                isVipActive -> VipGold.copy(alpha = 0.4f)
                                isUnlocked -> AccentSuccess.copy(alpha = 0.4f)
                                badgeText.contains("LIMIT", ignoreCase = true) -> AccentWarning.copy(alpha = 0.4f)
                                else -> NeonBlue.copy(alpha = 0.3f)
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = when {
                            isVipActive -> VipGold
                            isUnlocked -> AccentSuccess
                            badgeText.contains("LIMIT", ignoreCase = true) -> AccentWarning
                            else -> NeonBlue
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
