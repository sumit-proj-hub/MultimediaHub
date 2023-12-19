package com.example.multimediahub.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
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
import com.example.multimediahub.ViewBy

@Composable
fun FoldersScreen(displayInfo: FilesDisplayInfo, modifier: Modifier = Modifier) {
    if (!checkStoragePermissionAndShowMessage())
        return
    var mediaType by remember { mutableStateOf<SelectedMediaType?>(null) }
    var sortBy by remember { mutableStateOf<SortBy?>(null) }
    var folderOpened by remember { mutableStateOf<String?>(null) }
    val contentResolver = LocalContext.current.contentResolver
    if (folderOpened == null) {
        var folderList by remember { mutableStateOf(listOf<MediaInfo>()) }
        LaunchedEffect(key1 = listOf(displayInfo.selectedMediaType, displayInfo.sortBy)) {
            folderList = MediaInfo.getMediaFolders(
                displayInfo.selectedMediaType,
                displayInfo.sortBy,
                contentResolver
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
        when (displayInfo.viewBy) {
            ViewBy.List -> {
                val lazyListState = rememberLazyListState()
                LazyColumn(
                    modifier = modifier.simpleVerticalScrollbar(state = lazyListState),
                    state = lazyListState
                ) {
                    items(folderList) {
                        MediaListItem(it, modifier = Modifier.clickable {
                            folderOpened = it.filePath
                            mediaType = null
                        })
                    }
                }
            }

            ViewBy.Grid -> {
                val lazyGridState = rememberLazyGridState()
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(135.dp),
                    modifier = modifier.simpleVerticalScrollbar(lazyGridState),
                    state = lazyGridState
                ) {
                    items(folderList) {
                        MediaGridItem(it, modifier = Modifier.clickable {
                            folderOpened = it.filePath
                            mediaType = null
                        })
                    }
                }
            }
        }
        return
    }

    var fileList by remember { mutableStateOf(listOf<MediaInfo>()) }
    LaunchedEffect(key1 = listOf(displayInfo.selectedMediaType, displayInfo.sortBy)) {
        fileList = MediaInfo.getMediaList(
            contentResolver,
            displayInfo.selectedMediaType,
            displayInfo.sortBy,
            folderOpened
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
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            Text("Back")
        }
        when (displayInfo.viewBy) {
            ViewBy.List -> {
                val lazyListState = rememberLazyListState()
                LazyColumn(
                    modifier = modifier.simpleVerticalScrollbar(state = lazyListState),
                    state = lazyListState
                ) {
                    items(fileList) { MediaListItem(it) }
                }
            }

            ViewBy.Grid -> {
                val lazyGridState = rememberLazyGridState()
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(135.dp),
                    modifier = modifier.simpleVerticalScrollbar(lazyGridState),
                    state = lazyGridState
                ) {
                    items(fileList) { MediaGridItem(it) }
                }
            }
        }
    }
}