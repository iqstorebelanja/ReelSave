package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ads.AdsManager
import com.example.data.AppDatabase
import com.example.data.MediaRepository
import com.example.network.InstagramExtractor
import com.example.ui.MainViewModel
import com.example.ui.ReelsSaveApp
import com.example.ui.theme.ReelsSaveTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var repository: MediaRepository
    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize AdMob
        AdsManager.initialize(this)

        // 2. Initialize Database & Repository
        database = AppDatabase.getDatabase(this)
        repository = MediaRepository(database.downloadedMediaDao())

        // 3. Handle shared link from Instagram app
        handleIncomingIntent(intent)

        setContent {
            ReelsSaveTheme {
                ReelsSaveApp(
                    viewModel = viewModel,
                    activity = this
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
            extractAndFetchUrl(sharedText)
        }
    }

    private fun extractAndFetchUrl(text: String) {
        val parts = text.split("\\s+".toRegex())
        val foundUrl = parts.firstOrNull { InstagramExtractor.isValidInstagramUrl(it) } ?: text
        viewModel.onUrlChange(foundUrl)
        viewModel.fetchMedia()
    }
}
