package com.example.multimediahub.audioplayer

import android.net.Uri
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.multimediahub.MediaInfo
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.nio.file.Files

object AudioProperties {
    lateinit var sessionToken: SessionToken
    lateinit var mediaController: ListenableFuture<MediaController>
    var currentlyPlayingFile: File? by mutableStateOf(null)
    var audioUri: Uri? by mutableStateOf(null)
    var audioName: String? = null
    var currentPosition by mutableLongStateOf(0L)
    var audioLength by mutableLongStateOf(0L)
    var isPlaying by mutableStateOf(false)
    var pathIndexMap = mapOf<String, Int>()
    var indexPathMap = mapOf<Int, String>()

    fun compareAudioFile(file: File): Boolean {
        if (currentlyPlayingFile == null)
            return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.isSameFile(file.toPath(), currentlyPlayingFile!!.toPath())
        } else {
            file.canonicalPath == currentlyPlayingFile!!.canonicalPath
        }
    }

    fun setAudioPlaylist(audioList: List<MediaInfo>) {
        val player = mediaController.get()
        val pathIndexMap = mutableMapOf<String, Int>()
        val indexPathMap = mutableMapOf<Int, String>()
        audioList.forEachIndexed { index, mediaInfo ->
            pathIndexMap[mediaInfo.filePath] = index
            indexPathMap[index] = mediaInfo.filePath
        }
        this.pathIndexMap = pathIndexMap
        this.indexPathMap = indexPathMap
        player.setMediaItems(audioList.map { MediaItem.fromUri(Uri.fromFile(File(it.filePath))) })
    }
}