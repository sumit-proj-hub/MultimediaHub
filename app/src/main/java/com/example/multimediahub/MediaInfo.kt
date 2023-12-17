package com.example.multimediahub

import android.content.Context
import android.provider.MediaStore

enum class MediaType { Image, Audio, Video, PDF }

data class MediaInfo(
    val name: String,
    val mediaType: MediaType,
    val lastModified: Long,
    val size: Long,
    val filePath: String
)

fun getMediaList(
    context: Context,
    selectedMediaType: SelectedMediaType,
    sortBy: SortBy
): List<MediaInfo> {
    val mediaList = mutableListOf<MediaInfo>()
    val collection = MediaStore.Files.getContentUri("external")
    val projection = arrayOf(
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.MIME_TYPE,
        MediaStore.Files.FileColumns.DATE_MODIFIED,
        MediaStore.Files.FileColumns.SIZE,
        MediaStore.Files.FileColumns.DATA
    )
    val selection = when (selectedMediaType) {
        SelectedMediaType.All -> "mime_type LIKE 'image/%' OR mime_type LIKE 'audio/%' OR mime_type LIKE 'video/%' OR mime_type = 'application/pdf'"
        SelectedMediaType.Images -> "mime_type LIKE 'image/%'"
        SelectedMediaType.Music -> "mime_type LIKE 'audio/%'"
        SelectedMediaType.Videos -> "mime_type LIKE 'video/%'"
        SelectedMediaType.PDFs -> "mime_type = 'application/pdf'"
    }
    val sortOrder = when (sortBy) {
        SortBy.Name -> "${MediaStore.Files.FileColumns.DISPLAY_NAME} ASC"
        SortBy.Size -> "${MediaStore.Files.FileColumns.MEDIA_TYPE} DESC"
        SortBy.LastModified -> "${MediaStore.Files.FileColumns.DATE_MODIFIED}  DESC"
    }
    val query = context.contentResolver.query(collection, projection, selection, null, sortOrder)
    query?.use { cursor ->
        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
        val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
        val dateModifiedColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
        val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
        val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
        while (cursor.moveToNext()) {
            val mimeType = cursor.getString(mimeTypeColumn)
            mediaList += MediaInfo(
                name = cursor.getString(nameColumn),
                mediaType = when {
                    mimeType.startsWith("image/") -> MediaType.Image
                    mimeType.startsWith("video/") -> MediaType.Audio
                    mimeType.startsWith("audio/") -> MediaType.Video
                    else -> MediaType.PDF
                },
                lastModified = cursor.getLong(dateModifiedColumn),
                size = cursor.getLong(sizeColumn),
                filePath = cursor.getString(dataColumn)
            )
        }
    }
    return mediaList
}