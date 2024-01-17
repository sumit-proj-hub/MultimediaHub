package com.example.multimediahub

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.multimediahub.audioplayer.AudioProperties
import java.io.File


enum class MediaType { Image, Audio, Video, PDF }

class MediaInfo(
    val name: String,
    val mediaType: MediaType?,
    var lastModified: Long,
    var size: Long,
    val filePath: String,
) {
    @Composable
    @OptIn(ExperimentalGlideComposeApi::class)
    fun MediaIcon(modifier: Modifier = Modifier) {
        if (mediaType == null) {
            Icon(
                imageVector = Icons.Default.Folder,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "Folder",
                modifier = modifier
            )
            return
        }
        Box {
            var showSecondaryIcon = false
            when (mediaType) {
                MediaType.Audio -> {
                    val metadataRetriever = MediaMetadataRetriever()
                    metadataRetriever.setDataSource(
                        LocalContext.current,
                        Uri.fromFile(File(filePath))
                    )
                    val imageData: ByteArray? = metadataRetriever.embeddedPicture
                    if (imageData != null) {
                        Image(
                            bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                                .asImageBitmap(),
                            contentDescription = "Audio Thumbnail",
                            modifier = modifier
                        )
                        showSecondaryIcon = true
                    } else {
                        Icon(
                            imageVector = Icons.Default.AudioFile,
                            contentDescription = mediaType.toString(),
                            tint = colorResource(R.color.purple_200),
                            modifier = modifier
                        )
                    }
                }

                MediaType.PDF -> Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = mediaType.toString(),
                    tint = colorResource(R.color.light_red),
                    modifier = modifier
                )

                MediaType.Image -> GlideImage(
                    model = "file://$filePath",
                    contentDescription = null,
                    modifier = modifier
                )

                MediaType.Video -> {
                    GlideImage(
                        model = "file://$filePath",
                        contentDescription = null,
                        modifier = modifier
                    )
                    showSecondaryIcon = true
                }
            }
            if (showSecondaryIcon) {
                Icon(
                    imageVector = when (mediaType) {
                        MediaType.Audio -> Icons.Default.Audiotrack
                        else -> Icons.Default.Videocam
                    },
                    tint = Color.Black,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(Color.White, MaterialTheme.shapes.extraSmall)
                )
            }
        }
    }

    companion object {
        private fun getSelection(selectedMediaType: SelectedMediaType) = when (selectedMediaType) {
            SelectedMediaType.All -> "media_type IN (1,2,3) OR mime_type='application/pdf'"
            SelectedMediaType.Images -> "media_type=1"
            SelectedMediaType.Music -> "media_type=2"
            SelectedMediaType.Videos -> "media_type=3"
            SelectedMediaType.PDFs -> "mime_type='application/pdf'"
        }

        fun getMediaList(
            contentResolver: ContentResolver,
            selectedMediaType: SelectedMediaType,
            sortBy: SortBy,
            folderPath: String? = null,
        ): List<MediaInfo> {
            val mediaList = mutableListOf<MediaInfo>()
            val selection = if (folderPath == null) {
                getSelection(selectedMediaType)
            } else {
                "_data LIKE '${folderPath.replace("'", "''")}%' AND (${
                    getSelection(selectedMediaType)
                })"
            }
            contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                arrayOf("_display_name", "media_type", "date_modified", "_size", "_data"),
                selection,
                null,
                when (sortBy) {
                    SortBy.Name -> "_display_name ASC"
                    SortBy.Size -> "_size DESC"
                    SortBy.LastModified -> "date_modified DESC"
                }
            )?.use { cursor ->
                val nameColumn = cursor.getColumnIndexOrThrow("_display_name")
                val mediaTypeColumn = cursor.getColumnIndexOrThrow("media_type")
                val dateModifiedColumn = cursor.getColumnIndexOrThrow("date_modified")
                val sizeColumn = cursor.getColumnIndexOrThrow("_size")
                val dataColumn = cursor.getColumnIndexOrThrow("_data")
                while (cursor.moveToNext()) {
                    val filePath = cursor.getString(dataColumn)
                    mediaList += MediaInfo(
                        name = cursor.getString(nameColumn) ?: File(filePath).name,
                        mediaType = when (cursor.getInt(mediaTypeColumn)) {
                            1 -> MediaType.Image
                            2 -> MediaType.Audio
                            3 -> MediaType.Video
                            else -> MediaType.PDF
                        },
                        lastModified = cursor.getLong(dateModifiedColumn) * 1000,
                        size = cursor.getLong(sizeColumn),
                        filePath = filePath
                    )
                }
            }
            return mediaList
        }

        fun getMediaFolders(
            selectedMediaType: SelectedMediaType,
            sortBy: SortBy,
            contentResolver: ContentResolver,
        ): List<MediaInfo> {
            val folderMap = mutableMapOf<String, MediaInfo>()
            contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                arrayOf("_data", "date_modified", "_size"),
                getSelection(selectedMediaType),
                null,
                null
            )?.use { cursor ->
                val dataCol = cursor.getColumnIndexOrThrow("_data")
                val lastModifiedCol = cursor.getColumnIndexOrThrow("date_modified")
                val sizeCol = cursor.getColumnIndexOrThrow("_size")
                while (cursor.moveToNext()) {
                    val lastModified = cursor.getLong(lastModifiedCol) * 1000
                    val size = cursor.getLong(sizeCol)
                    val parentFile = File(cursor.getString(dataCol)).parentFile
                    val path = parentFile!!.path
                    if (folderMap.containsKey(path)) {
                        if (folderMap[path]!!.lastModified < lastModified)
                            folderMap[path]!!.lastModified = lastModified
                        folderMap[path]!!.size += size
                    } else {
                        folderMap[path] = MediaInfo(
                            name = parentFile.name,
                            mediaType = null,
                            lastModified = lastModified,
                            size = size,
                            filePath = path
                        )
                    }
                }
            }
            return when (sortBy) {
                SortBy.Name -> folderMap.values.sortedBy { it.name }
                SortBy.LastModified -> folderMap.values.sortedByDescending { it.lastModified }
                SortBy.Size -> folderMap.values.sortedByDescending { it.size }
            }
        }
    }
}

class ReducedMediaInfo(
    val name: String,
    val mediaType: MediaType,
    val filePath: String,
) {
    @Composable
    fun MediaIcon(modifier: Modifier = Modifier) {
        Icon(
            imageVector = when (mediaType) {
                MediaType.Image -> Icons.Default.Image
                MediaType.Audio -> Icons.Default.AudioFile
                MediaType.Video -> Icons.Default.VideoFile
                MediaType.PDF -> Icons.Default.PictureAsPdf
            },
            tint = when (mediaType) {
                MediaType.Image -> Color.Cyan
                MediaType.Audio -> colorResource(R.color.purple_200)
                MediaType.Video -> colorResource(R.color.orange)
                MediaType.PDF -> colorResource(R.color.light_red)
            }, contentDescription = mediaType.toString(), modifier = modifier
        )
    }

    companion object {
        fun getReducedMediaList(contentResolver: ContentResolver): List<ReducedMediaInfo> {
            val mediaList = mutableListOf<ReducedMediaInfo>()
            contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                arrayOf("_display_name", "media_type", "_data"),
                "media_type IN (1,2,3) OR mime_type='application/pdf'",
                null,
                "date_modified"
            )?.use {
                val nameCol = it.getColumnIndexOrThrow("_display_name")
                val mediaTypeCol = it.getColumnIndexOrThrow("media_type")
                val dataCol = it.getColumnIndexOrThrow("_data")
                while (it.moveToNext()) {
                    val filePath = it.getString(dataCol)
                    mediaList += ReducedMediaInfo(
                        name = it.getString(nameCol) ?: File(filePath).name,
                        mediaType = when (it.getInt(mediaTypeCol)) {
                            1 -> MediaType.Image
                            2 -> MediaType.Audio
                            3 -> MediaType.Video
                            else -> MediaType.PDF
                        },
                        filePath = filePath
                    )
                }
            }
            return mediaList
        }
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            .use {
                if (it != null && it.moveToFirst())
                    result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
    }
    if (result == null) {
        result = uri.lastPathSegment
    }
    return result
}

fun getUriAndNameFromIntent(context: Context, intent: Intent): Pair<Uri?, String?> {
    var uri: Uri? = null
    var fileName: String? = null
    if (intent.action == Intent.ACTION_VIEW) {
        uri = intent.data
        if (uri != null)
            fileName = getFileNameFromUri(context, uri)
    } else {
        val path = intent.extras?.getString("path")
        if (path != null) {
            val file = File(path)
            uri = Uri.fromFile(file)
            fileName = file.name
        }
    }
    return Pair(uri, fileName)
}

fun setupAudioFromIntent(context: Context, intent: Intent): Pair<String?, MediaController> {
    val mediaController = AudioProperties.mediaController.get()
    val audioName: String?
    if (intent.action == Intent.ACTION_VIEW) {
        val uri = intent.data ?: throw Exception("Audio Not Found")
        audioName = getFileNameFromUri(context, uri)
        AudioProperties.audioUri = uri
        AudioProperties.audioName = audioName
        mediaController.setMediaItem(MediaItem.fromUri(uri))
        mediaController.seekTo(0L)
        mediaController.play()
    } else {
        audioName = AudioProperties.currentlyPlayingFile?.name
    }
    return Pair(audioName, mediaController)
}