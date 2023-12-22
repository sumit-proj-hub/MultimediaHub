package com.example.multimediahub.videoplayer

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerView
import androidx.media3.ui.R
import com.example.multimediahub.screens.MessageText
import java.io.File
import java.util.UUID

class VideoPlayerActivity : ComponentActivity() {
    private var isLandscape: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val path = intent.extras?.getString("path")
        isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        setContent {
            if (path == null) {
                MessageText("Failed to load video.")
            } else {
                Content(path)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    @Composable
    private fun Content(path: String) {
        var isControllerVisible by remember { mutableStateOf(true) }
        Surface(color = Color.Black) {
            Box {
                VideoPlayer(videoUri = Uri.fromFile(File(path)), controllerVisibilityListener = {
                    isControllerVisible = it
                })
                if (isControllerVisible) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable { finish() }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = File(path).name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    @Composable
    private fun VideoPlayer(
        videoUri: Uri,
        controllerVisibilityListener: (Boolean) -> Unit,
        modifier: Modifier = Modifier
    ) {
        val player = ExoPlayer.Builder(this)
                .setSeekForwardIncrementMs(10000L)
                .setSeekBackIncrementMs(10000L)
                .build()
        var currentTime by rememberSaveable { mutableStateOf(0L) }
        var shouldPlay by rememberSaveable { mutableStateOf(true) }
        var lifecycle by remember { mutableStateOf(Lifecycle.Event.ON_CREATE) }
        val lifecycleOwner = LocalLifecycleOwner.current

        player.setMediaItem(MediaItem.fromUri(videoUri))
        MediaSession.Builder(this, player).setId(UUID.randomUUID().toString()).build()

        DisposableEffect(Unit) {
            player.prepare()
            player.seekTo(currentTime)
            if (shouldPlay)
                player.play()
            onDispose {
                currentTime = player.currentPosition
                shouldPlay = player.isPlaying
                player.release()
            }
        }

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                lifecycle = event
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        AndroidView(
            factory = { context ->
                PlayerView(context).also { view ->
                    view.player = player
                    view.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener {
                        controllerVisibilityListener(it == View.VISIBLE)
                    })
                    view.setShowNextButton(false)
                    view.setShowPreviousButton(false)
                    setFullscreenIcon(view)
                    view.setFullscreenButtonClickListener {
                        requestedOrientation = if (isLandscape == true)
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                        else
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                    view.controllerShowTimeoutMs = 4000
                }
            },
            update = {
                when (lifecycle) {
                    Lifecycle.Event.ON_PAUSE -> {
                        it.onPause()
                        it.player?.pause()
                    }

                    Lifecycle.Event.ON_RESUME -> {
                        it.onResume()
                    }

                    else -> Unit
                }
                setFullscreenIcon(it)
            }, modifier = modifier.fillMaxSize()
        )
    }

    @SuppressLint("PrivateResource")
    private fun setFullscreenIcon(playerView: PlayerView) {
        val btn = playerView.rootView.findViewById<ImageButton>(R.id.exo_fullscreen)
        if (isLandscape == true) {
            btn.setImageDrawable(resources.getDrawable(R.drawable.exo_ic_fullscreen_exit, theme))
        } else {
            btn.setImageDrawable(resources.getDrawable(R.drawable.exo_ic_fullscreen_enter, theme))
        }
    }
}