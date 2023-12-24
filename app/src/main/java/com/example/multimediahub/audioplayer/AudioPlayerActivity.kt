package com.example.multimediahub.audioplayer

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.multimediahub.screens.MessageText
import java.io.File

class AudioPlayerActivity : ComponentActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val path = intent.extras?.getString("path")
        if (path != null) {
            player = ExoPlayer.Builder(this).build()
            player.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(path))))
            mediaSession = MediaSession.Builder(this@AudioPlayerActivity, player).build()
            player.prepare()
        }
        setContent {
            if (path == null)
                MessageText("Failed to load image.")
            else
                Content(path)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::player.isInitialized) mediaSession.release()
        if (this::player.isInitialized) player.release()
    }

    @OptIn(UnstableApi::class)
    @Composable
    private fun Content(path: String) {
        var currentPosition by rememberSaveable { mutableStateOf(0L) }
        var audioLength by rememberSaveable { mutableStateOf(0L) }
        var isPlaying by rememberSaveable { mutableStateOf(true) }

        DisposableEffect(Unit) {
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == ExoPlayer.STATE_READY)
                        audioLength = player.duration
                    else if (playbackState == ExoPlayer.STATE_ENDED) {
                        player.seekTo(0L)
                        player.pause()
                        isPlaying = false
                    }
                }

                override fun onIsPlayingChanged(playState: Boolean) {
                    isPlaying = playState
                }
            }
            player.addListener(listener)
            player.seekTo(currentPosition)
            if (isPlaying) player.play()
            onDispose {
                player.removeListener(listener)
            }
        }

        DisposableEffect(isPlaying) {
            var handler: Handler? = Handler(Looper.getMainLooper())
            val runnable = object : Runnable {
                override fun run() {
                    currentPosition = player.currentPosition
                    handler?.postDelayed(this, 200)
                }
            }
            if (isPlaying)
                handler?.postDelayed(runnable, 0)
            onDispose {
                handler = null
            }
        }

        Surface(color = Color.Black) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    Modifier
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                        .fillMaxWidth(),
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
                AudioThumbnail(
                    path = path,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                )
                AudioController(
                    currentPosition,
                    audioLength,
                    isPlaying,
                    setPosition = { player.seekTo(it) },
                    setPlayState = { if (it) player.play() else player.pause() }
                )
            }
        }
    }

    @Composable
    private fun AudioController(
        currentPosition: Long,
        audioLength: Long,
        isPlaying: Boolean,
        setPosition: (Long) -> Unit,
        setPlayState: (Boolean) -> Unit,
        modifier: Modifier = Modifier
    ) {
        var lastPosition by remember { mutableStateOf<Long?>(null) }
        Column(modifier = modifier.padding(8.dp)) {
            Slider(
                value = if (lastPosition == null) {
                    if (audioLength == 0L) 0f else currentPosition.toFloat() / audioLength
                } else {
                    lastPosition!!.toFloat() / audioLength
                },
                onValueChange = {
                    if (lastPosition == null) {
                        lastPosition = (it * audioLength).toLong()
                        setPosition(lastPosition!!)
                        lastPosition = null
                    }
                })
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = millisecondsToTimeString(currentPosition),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = millisecondsToTimeString(audioLength),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = "Rewind",
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            val newPosition = currentPosition - 10000L
                            setPosition(if (newPosition >= 0L) newPosition else 0L)
                        }
                )
                Icon(
                    imageVector = if (isPlaying) {
                        Icons.Default.PauseCircleFilled
                    } else {
                        Icons.Default.PlayCircleFilled
                    },
                    contentDescription = "Pause/Play",
                    tint = Color.White,
                    modifier = Modifier
                        .size(64.dp)
                        .clickable {
                            setPlayState(!isPlaying)
                        }
                )
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Forward",
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            val newPosition = currentPosition + 10000L
                            setPosition(if (newPosition <= audioLength) newPosition else audioLength)
                        }
                )
            }
        }
    }

    @Composable
    private fun AudioThumbnail(path: String, modifier: Modifier = Modifier) {
        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(path)
        val imageData = metadataRetriever.embeddedPicture
        if (imageData != null) {
            Image(
                bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                    .asImageBitmap(),
                contentDescription = "Audio Thumbnail",
                modifier = modifier
            )
            return
        }
        Icon(
            imageVector = Icons.Default.Audiotrack,
            contentDescription = "Audio Icon",
            modifier = modifier.background(Color.DarkGray),
        )
    }

    private fun millisecondsToTimeString(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        val remainingSeconds = seconds % 60
        val remainingMinutes = minutes % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, remainingMinutes, remainingSeconds)
        } else {
            String.format("%02d:%02d", remainingMinutes, remainingSeconds)
        }
    }
}

