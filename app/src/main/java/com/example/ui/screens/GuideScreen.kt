package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SubtleGridBackground
import com.example.ui.theme.GlassBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun GuideScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    SubtleGridBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Header
            item {
                Column {
                    Text(
                        text = "Documentation & Workflow",
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Three step extraction guide and Quality Gate specifications",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Step 1
            item {
                GuideStepCard(
                    stepNumber = "01",
                    icon = Icons.Default.Share,
                    title = "Copy Video URL",
                    description = "From Instagram, TikTok, X, or YouTube, tap the Share icon on any video or story and select 'Copy Link'."
                )
            }

            // Step 2
            item {
                GuideStepCard(
                    stepNumber = "02",
                    icon = Icons.Default.ContentCopy,
                    title = "Paste into ALLVID",
                    description = "Open ALLVID and tap the centered input or 'Paste' pill. The URL is resolved instantly by our parallel parser."
                )
            }

            // Step 3
            item {
                GuideStepCard(
                    stepNumber = "03",
                    icon = Icons.Default.Download,
                    title = "Select Quality & Download",
                    description = "Choose between 720p (Free), 1080p Full HD (1 sponsor ad), or 4K Ultra HD (2 sponsor ads). VIP members bypass all ads."
                )
            }

            // Open Instagram CTA
            item {
                Button(
                    onClick = { openInstagramApp(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Launch Instagram",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Quality Gate Architecture
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "System FAQ",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    FaqCard(
                        question = "What are the Quality Gate rules?",
                        answer = "720p standard video and MP3 audio are completely free with zero ads. 1080p Full HD unlocks by watching 1 rewarded sponsor ad. 4K Ultra HD unlocks after 2 rewarded sponsor ads. VIP eliminates all gates."
                    )

                    FaqCard(
                        question = "Where are saved files stored?",
                        answer = "Media files are stored in your device's primary download directory under ALLVID. They are available offline in the Media Vault tab."
                    )

                    FaqCard(
                        question = "What platforms are supported?",
                        answer = "20+ platforms including Instagram Reels & Stories, TikTok, X (Twitter), YouTube, Facebook, Reddit, and Threads."
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}

private fun openInstagramApp(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("instagram://app"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com"))
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(webIntent)
    }
}

@Composable
private fun GuideStepCard(
    stepNumber: String,
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = GlassBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, NeonBlue.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NeonBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stepNumber,
                        color = TextTertiary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun FaqCard(
    question: String,
    answer: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = GlassBg),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = NeonBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = question,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = answer,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
