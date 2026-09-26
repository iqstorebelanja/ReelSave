package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.GlassBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VipGold

@Composable
fun AllVidTopBar(
    isVipActive: Boolean,
    onToggleVip: (Boolean) -> Unit,
    onVipDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_dot")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: ALLVID Logo (Lucide / Linear geometric style)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, NeonBlue.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "ALLVID",
                    tint = NeonBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ALLVID",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NeonBlue.copy(alpha = 0.15f))
                            .border(0.5.dp, NeonBlue.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "PRO",
                            color = NeonBlue,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Center: "20+ Platforms Supported" (Linear style pill)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(GlassBg)
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AccentSuccess.copy(alpha = pulseAlpha))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "20+ Platforms Supported",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Right: "VIP $29/mo" Toggle Switch (Premium Switch)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isVipActive) VipGold.copy(alpha = 0.15f) else GlassBg)
                .border(
                    width = 1.dp,
                    color = if (isVipActive) VipGold.copy(alpha = 0.6f) else GlassBorder,
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { onVipDetailsClick() }
                .padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = null,
                    tint = if (isVipActive) VipGold else TextTertiary,
                    modifier = Modifier.size(13.dp)
                )

                Column {
                    Text(
                        text = if (isVipActive) "VIP ON" else "VIP",
                        color = if (isVipActive) VipGold else TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$29/mo",
                        color = TextTertiary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Micro switch
                Switch(
                    checked = isVipActive,
                    onCheckedChange = { onToggleVip(it) },
                    modifier = Modifier.scale(0.7f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = VipGold,
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f),
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }
        }
    }
}
