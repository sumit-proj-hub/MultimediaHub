package com.example.multimediahub.audioplayer

import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.multimediahub.screens.MessageText
import com.example.multimediahub.setupAudioFromIntent
import com.google.common.util.concurrent.MoreExecutors
import kotlin.system.exitProcess

class AudioPlayerActivity : ComponentActivity() {
    private var shouldReleaseController: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkoutAudioSession {
            try {
                val (fileName, mediaController) = setupAudioFromIntent(this, intent)
                setContent {
                    Content(mediaController, fileName ?: AudioProperties.audioName ?: "Audio")
                }
            } catch (_: Exception) {
                setContent {
                    MessageText("Failed to load audio.")
                }
            }
        }
    }

    private fun checkoutAudioSession(next: () -> Unit) {
        if (intent.action == Intent.ACTION_VIEW) {
            shouldReleaseController = true
            AudioProperties.sessionToken =
                SessionToken(this, ComponentName(this, AudioPlayerService::class.java))
            AudioProperties.mediaController = MediaController
                .Builder(this, AudioProperties.sessionToken)
                .buildAsync()
            AudioProperties.mediaController.addListener({ next() }, MoreExecutors.directExecutor())
        } else next()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (shouldReleaseController) {
            AudioProperties.mediaController.get().release()
            exitProcess(0)
        }
    }

    @OptIn(UnstableApi::class)
    @Composable
    private fun Content(mediaController: MediaController, fileName: String) {
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
                        text = fileName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                AudioThumbnail(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                )
                AudioController(mediaController)
            }
        }
    }

    @Composable
    private fun AudioController(mediaController: MediaController, modifier: Modifier = Modifier) {
        Column(modifier = modifier.padding(8.dp)) {
            Slider(
                value = if (AudioProperties.audioLength == 0L) {
                    0f
                } else {
                    AudioProperties.currentPosition.toFloat() / AudioProperties.audioLength
                },
                onValueChange = {
                    mediaController.seekTo((it * AudioProperties.audioLength).toLong())
                }
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = millisecondsToTimeString(AudioProperties.currentPosition),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = millisecondsToTimeString(AudioProperties.audioLength),
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
                            val newPosition = AudioProperties.currentPosition - 10000L
                            mediaController.seekTo(if (newPosition >= 0L) newPosition else 0L)
                        }
                )
                Icon(
                    imageVector = if (AudioProperties.isPlaying) {
                        Icons.Default.PauseCircleFilled
                    } else {
                        Icons.Default.PlayCircleFilled
                    },
                    contentDescription = "Pause/Play",
                    tint = Color.White,
                    modifier = Modifier
                        .size(64.dp)
                        .clickable {
                            if (mediaController.isPlaying)
                                mediaController.pause()
                            else
                                mediaController.play()
                        }
                )
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Forward",
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            val newPosition = AudioProperties.currentPosition + 10000L
                            mediaController.seekTo(
                                if (newPosition <= AudioProperties.audioLength) newPosition
                                else AudioProperties.audioLength
                            )
                        }
                )
            }
        }
    }

    @Composable
    private fun AudioThumbnail(modifier: Modifier = Modifier) {
        val metadataRetriever = MediaMetadataRetriever()
        val audioUri = if (intent.action == Intent.ACTION_VIEW) intent.data
        else {
            if (AudioProperties.currentlyPlayingFile != null)
                Uri.fromFile(AudioProperties.currentlyPlayingFile)
            else
                AudioProperties.audioUri
        }
        val imageData: ByteArray? = if (audioUri != null) {
            metadataRetriever.setDataSource(this, audioUri)
            metadataRetriever.embeddedPicture
        } else null
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

