package com.example.multimediahub.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multimediahub.FilesDisplayInfo
import com.example.multimediahub.MediaIcon
import com.example.multimediahub.MediaInfo
import com.example.multimediahub.SelectedMediaType
import com.example.multimediahub.SortBy
import com.example.multimediahub.ViewBy
import com.example.multimediahub.getMediaList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FilesScreen(displayInfo: FilesDisplayInfo, modifier: Modifier = Modifier) {
    if (!checkStoragePermissionAndShowMessage())
        return
    var fileList by remember { mutableStateOf(listOf<MediaInfo>()) }
    var mediaType by remember { mutableStateOf<SelectedMediaType?>(null) }
    var sortBy by remember { mutableStateOf<SortBy?>(null) }
    val context = LocalContext.current
    LaunchedEffect(key1 = listOf(displayInfo.selectedMediaType, displayInfo.sortBy)) {
        fileList = getMediaList(context, displayInfo.selectedMediaType, displayInfo.sortBy)
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
                items(fileList) { ListItem(it) }
            }
        }

        ViewBy.Grid -> {
            val lazyGridState = rememberLazyGridState()
            LazyVerticalGrid(
                columns = GridCells.Adaptive(140.dp),
                modifier = modifier.simpleVerticalScrollbar(lazyGridState),
                state = lazyGridState
            ) {
                items(fileList) { GridItem(it) }
            }
        }
    }
}

@Composable
fun GridItem(mediaInfo: MediaInfo, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(6.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column {
            MediaIcon(
                mediaInfo = mediaInfo, modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
            )
            Text(
                text = mediaInfo.name,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 3.dp)
            )
        }
    }
}

@Composable
private fun ListItem(mediaInfo: MediaInfo, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
            ) {
                MediaIcon(
                    mediaInfo, modifier = Modifier
                        .width(75.dp)
                        .fillMaxHeight()
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .defaultMinSize(minHeight = 50.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = mediaInfo.name, fontSize = 18.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = formatMilliseconds(mediaInfo.lastModified), color = Color.Gray)
                        Text(text = formatFileSize(mediaInfo.size), color = Color.Gray)
                    }
                }
            }
        }
    }
}

private fun formatMilliseconds(milliseconds: Long): String {
    val date = Date(milliseconds)
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return formatter.format(date)
}

private fun formatFileSize(size: Long): String {
    val units = listOf("Bytes", "KiB", "MiB", "GiB")
    var value = size.toDouble()
    var index = 0
    while (value >= 1024 && index < units.size - 1) {
        value /= 1024
        index++
    }
    val formattedValue = String.format("%.2f", value)
    return "$formattedValue ${units[index]}"
}