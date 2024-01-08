package com.example.multimediahub.audioplayer

import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.nio.file.Files

object AudioProperties {
    var sessionToken: SessionToken? = null
    lateinit var mediaController: ListenableFuture<MediaController>
    var currentlyPlayingFile: File? by mutableStateOf(null)
    var currentPosition by mutableLongStateOf(0L)
    var audioLength by mutableLongStateOf(0L)
    var isPlaying by mutableStateOf(false)

    fun compareAudioFile(file: File): Boolean {
        if (currentlyPlayingFile == null)
            return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.isSameFile(file.toPath(), currentlyPlayingFile!!.toPath())
        } else {
            file.canonicalPath == currentlyPlayingFile!!.canonicalPath
        }
    }
}