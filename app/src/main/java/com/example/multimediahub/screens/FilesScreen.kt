package com.example.multimediahub.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.multimediahub.FilesDisplayInfo
import com.example.multimediahub.MediaInfo
import com.example.multimediahub.SelectedMediaType
import com.example.multimediahub.SortBy
import com.example.multimediahub.ViewBy

@Composable
fun FilesScreen(displayInfo: FilesDisplayInfo, modifier: Modifier = Modifier) {
    if (!checkStoragePermissionAndShowMessage())
        return
    var fileList by remember { mutableStateOf(listOf<MediaInfo>()) }
    var mediaType by remember { mutableStateOf<SelectedMediaType?>(null) }
    var sortBy by remember { mutableStateOf<SortBy?>(null) }
    val contentResolver = LocalContext.current.contentResolver
    LaunchedEffect(key1 = listOf(displayInfo.selectedMediaType, displayInfo.sortBy)) {
        fileList = MediaInfo.getMediaList(
            contentResolver,
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