package com.example.multimediahub.audioplayer

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import java.io.File

class AudioPlayerService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var playerListener: Player.Listener
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession
            .Builder(this, player)
            .setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, AudioPlayerActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
        playerListener = object : Player.Listener {
            private val handler = Handler(Looper.getMainLooper())
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_READY)
                    AudioProperties.audioLength = player.duration
            }

            override fun onIsPlayingChanged(playState: Boolean) {
                AudioProperties.isPlaying = playState
                val runnable = object : Runnable {
                    override fun run() {
                        AudioProperties.currentPosition = player.currentPosition
                        if (playState)
                            handler.postDelayed(this, 200)
                    }
                }
                if (playState)
                    handler.postDelayed(runnable, 0)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                if (AudioProperties.indexPathMap.isEmpty())
                    return
                val audioPath = AudioProperties.indexPathMap[player.currentMediaItemIndex]
                AudioProperties.currentlyPlayingFile =
                    if (audioPath != null) File(audioPath) else null
                AudioProperties.audioUri = Uri.fromFile(AudioProperties.currentlyPlayingFile)
                AudioProperties.audioLength = player.duration
            }
        }
        player.addListener(playerListener)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player?.playWhenReady == true)
            player.pause()
        if (this::playerListener.isInitialized)
            player?.removeListener(playerListener)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.run {
            player.release()
            release()
        }
    }
}