package com.example.ui.components

import android.content.Context
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.downloader.DownloadService
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VideoPlayerDialog(
    videoUrlOrPath: String,
    title: String,
    author: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isBuffering by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = PureBlack
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Glassmorphic Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xCC000000))
                            .border(0.5.dp, GlassBorder)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "@$author",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = title,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }

                        // Share
                        IconButton(
                            onClick = {
                                DownloadService.shareFile(context, videoUrlOrPath)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // External Player
                        IconButton(
                            onClick = {
                                DownloadService.openInSystemPlayer(context, videoUrlOrPath)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open in player",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Video Player View in Center
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(9f / 16f)
                                .clip(RoundedCornerShape(12.dp)),
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    val mediaController = MediaController(ctx)
                                    mediaController.setAnchorView(this)
                                    setMediaController(mediaController)

                                    setOnPreparedListener { mp ->
                                        isBuffering = false
                                        mp.isLooping = true
                                        start()
                                    }
                                    setOnErrorListener { _, _, _ ->
                                        isBuffering = false
                                        false
                                    }
                                    setVideoURI(Uri.parse(videoUrlOrPath))
                                }
                            }
                        )

                        if (isBuffering) {
                            CircularProgressIndicator(
                                color = NeonBlue,
                                modifier = Modifier.size(44.dp),
                                strokeWidth = 3.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
