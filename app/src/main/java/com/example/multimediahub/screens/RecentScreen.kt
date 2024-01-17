package com.example.multimediahub.screens

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.multimediahub.FilesDisplayInfo
import com.example.multimediahub.MediaInfo
import com.example.multimediahub.MediaType
import com.example.multimediahub.SelectedMediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

@Composable
fun RecentScreen(
    displayInfo: FilesDisplayInfo,
    scrollDirectionListener: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!checkStoragePermissionAndShowMessage())
        return
    var fileList by remember { mutableStateOf(listOf<MediaInfo>()) }
    var mediaType by remember { mutableStateOf<SelectedMediaType?>(null) }
    val context = LocalContext.current
    LaunchedEffect(displayInfo.selectedMediaType) {
        fileList = getRecentMedia(context, displayInfo.selectedMediaType) {
            mediaType = displayInfo.selectedMediaType
        }
    }
    if (mediaType != displayInfo.selectedMediaType) {
        MessageText("Loading ...")
        return
    }
    if (fileList.isEmpty()) {
        MessageText(msg = "No Items")
        return
    }
    Column {
        val recentFile = File("${context.filesDir}/recent.map")
        if (recentFile.exists()) {
            Row(
                modifier = Modifier
                    .padding(start = 8.dp, end = 4.dp, bottom = 4.dp)
                    .clickable {
                        recentFile.delete()
                        fileList = listOf()
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                Text("Clear Recent")
            }
        }
        ShowAllMedia(
            displayInfo = displayInfo,
            listState = rememberLazyListState(),
            gridState = rememberLazyGridState(),
            onClick = { onMediaClick(context, it.mediaType!!, it.filePath) },
            list = fileList,
            scrollDirectionListener = scrollDirectionListener,
            modifier = modifier.animateContentSize(),
        )
    }
}

@Suppress("UNCHECKED_CAST")
private suspend fun getRecentMedia(
    context: Context,
    filterMediaType: SelectedMediaType,
    onPostExecute: () -> Unit
): List<MediaInfo> {
    val recentFile = File("${context.filesDir}/recent.map")
    if (!recentFile.exists()) {
        onPostExecute()
        return listOf()
    }
    val recentList = mutableListOf<MediaInfo>()
    withContext(Dispatchers.IO) {
        ObjectInputStream(FileInputStream(recentFile)).use { inputStream ->
            val recentMap =
                (inputStream.readObject() as Map<String, Pair<MediaType, Long>>).toMutableMap()
            recentMap.toList().filter {
                val mediaType = it.second.first
                when (filterMediaType) {
                    SelectedMediaType.All -> true
                    SelectedMediaType.Images -> MediaType.Image == mediaType
                    SelectedMediaType.Music -> MediaType.Audio == mediaType
                    SelectedMediaType.Videos -> MediaType.Video == mediaType
                    SelectedMediaType.PDFs -> MediaType.PDF == mediaType
                }
            }.sortedByDescending { (_, pair) ->
                pair.second
            }.map {
                val file = File(it.first)
                if (!file.exists()) {
                    recentMap.remove(it.first)
                    return@map
                }
                recentList += MediaInfo(
                    name = file.name,
                    mediaType = it.second.first,
                    lastModified = file.lastModified(),
                    size = file.length(),
                    filePath = it.first
                )
            }
            ObjectOutputStream(FileOutputStream(recentFile)).use {
                it.writeObject(recentMap)
            }
        }
    }
    onPostExecute()
    return recentList
}