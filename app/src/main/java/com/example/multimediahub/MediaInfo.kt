package com.example.multimediahub

import android.content.ContentResolver
import android.provider.MediaStore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import java.io.File

enum class MediaType { Image, Audio, Video, PDF }

class MediaInfo(
    val name: String,
    val mediaType: MediaType?,
    var lastModified: Long,
    var size: Long,
    val filePath: String
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
        when (mediaType) {
            MediaType.Audio -> Icon(
                imageVector = Icons.Default.AudioFile,
                contentDescription = mediaType.toString(),
                tint = colorResource(R.color.purple_200),
                modifier = modifier
            )

            MediaType.PDF -> Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = mediaType.toString(),
                tint = colorResource(R.color.light_red),
                modifier = modifier
            )

            MediaType.Image, MediaType.Video -> GlideImage(
                model = "file://$filePath",
                contentDescription = null,
                modifier = modifier
            )
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
            folderPath: String? = null
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
            contentResolver: ContentResolver
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