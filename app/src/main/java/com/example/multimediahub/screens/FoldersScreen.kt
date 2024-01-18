package com.example.multimediahub.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.multimediahub.SelectedMediaType
import com.example.multimediahub.SortBy

@Composable
fun FoldersScreen(
    displayInfo: FilesDisplayInfo,
    scrollDirectionListener: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!checkStoragePermissionAndShowMessage()) return
    var mediaType by remember { mutableStateOf<SelectedMediaType?>(null) }
    var sortBy by remember { mutableStateOf<SortBy?>(null) }
    var folderOpened by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    if (folderOpened == null) {
        var folderList by remember { mutableStateOf(listOf<MediaInfo>()) }
        LaunchedEffect(displayInfo.selectedMediaType, displayInfo.sortBy) {
            folderList = MediaInfo.getMediaFolders(
                displayInfo.selectedMediaType, displayInfo.sortBy, context.contentResolver
            )
            mediaType = displayInfo.selectedMediaType
            sortBy = displayInfo.sortBy
        }
        if (mediaType != displayInfo.selectedMediaType || sortBy != displayInfo.sortBy) {
            MessageText("Loading ...")
            return
        }
        if (folderList.isEmpty()) {
            MessageText(msg = "No Items")
            return
        }
        ShowAllMedia(
            displayInfo = displayInfo,
            listState = rememberLazyListState(),
            gridState = rememberLazyGridState(),
            onClick = {
                folderOpened = it.filePath
                mediaType = null
            },
            list = folderList,
            scrollDirectionListener = scrollDirectionListener,
            modifier = modifier.animateContentSize()
        )
        return
    }

    var fileList by remember { mutableStateOf(listOf<MediaInfo>()) }
    LaunchedEffect(displayInfo.selectedMediaType, displayInfo.sortBy) {
        fileList = MediaInfo.getMediaList(
            context.contentResolver, displayInfo.selectedMediaType, displayInfo.sortBy, folderOpened
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
    Column {
        Row(
            modifier = Modifier
                .padding(start = 8.dp, end = 4.dp, top = 12.dp, bottom = 4.dp)
                .clickable {
                    folderOpened = null
                    mediaType = null
                }, verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            Text("Back")
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
            modifier = modifier
        )
    }
}