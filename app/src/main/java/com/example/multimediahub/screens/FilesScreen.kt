package com.example.multimediahub.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.multimediahub.FilesDisplayInfo
import com.example.multimediahub.MediaInfo
import com.example.multimediahub.SelectedMediaType
import com.example.multimediahub.SortBy

@Composable
fun FilesScreen(
    displayInfo: FilesDisplayInfo,
    scrollDirectionListener: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!checkStoragePermissionAndShowMessage())
        return
    var fileList by remember { mutableStateOf(listOf<MediaInfo>()) }
    var mediaType by remember { mutableStateOf<SelectedMediaType?>(null) }
    var sortBy by remember { mutableStateOf<SortBy?>(null) }
    val context = LocalContext.current
    LaunchedEffect(displayInfo.selectedMediaType, displayInfo.sortBy) {
        fileList = MediaInfo.getMediaList(
            context.contentResolver,
            displayInfo.selectedMediaType,
            displayInfo.sortBy
        )
        mediaType = displayInfo.selectedMediaType
        sortBy = displayInfo.sortBy
    }
    if (mediaType != displayInfo.selectedMediaType || sortBy != displayInfo.sortBy) {
        MessageText("Loading ...")
        return
    }
    if (fileList.isEmpty()) {
        MessageText(msg = "No Items")
        return
    }
    ShowAllMedia(
        displayInfo = displayInfo,
        listState = rememberLazyListState(),
        gridState = rememberLazyGridState(),
        onClick = if (displayInfo.selectedMediaType == SelectedMediaType.Music) { {
            onAudioClick(context, fileList, it.filePath)
        } } else { {
            onMediaClick(context, it.mediaType!!, it.filePath)
        } },
        list = fileList,
        scrollDirectionListener = scrollDirectionListener,
        modifier = modifier.animateContentSize()
    )
}