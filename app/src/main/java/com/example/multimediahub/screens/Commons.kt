package com.example.multimediahub.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import com.example.multimediahub.MediaInfo
import com.example.multimediahub.MediaType
import com.example.multimediahub.SortBy
import com.example.multimediahub.ViewBy
import com.example.multimediahub.audioplayer.AudioPlayerActivity
import com.example.multimediahub.audioplayer.AudioProperties
import com.example.multimediahub.imageviewer.ImageViewerActivity
import com.example.multimediahub.pdfviewer.PDFViewerActivity
import com.example.multimediahub.videoplayer.VideoPlayerActivity
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.LazyGridVerticalScrollbar
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun checkStoragePermissionAndShowMessage(): Boolean {
    val isPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        LocalContext.current.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
    if (!isPermissionGranted) {
        MessageText("Permissions Not Granted")
    }
    return isPermissionGranted
}

@Composable
fun MessageText(msg: String) {
    Text(
        text = msg,
        fontSize = 24.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    )
}

@Composable
fun MediaGridItem(mediaInfo: MediaInfo, modifier: Modifier = Modifier) {
    val showAudioControls =
        mediaInfo.mediaType == MediaType.Audio && AudioProperties.compareAudioFile(File(mediaInfo.filePath))
    val modifierClickable = if (showAudioControls) {
        val context = LocalContext.current
        modifier.clickable { showAudioPlayer(context) }
    } else modifier
    Surface(
        modifier = modifierClickable.padding(6.dp),
        shape = MaterialTheme.shapes.small,
        border = if (showAudioControls)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null,
        color = if (showAudioControls)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column {
            Box {
                mediaInfo.MediaIcon(
                    Modifier
                        .height(100.dp)
                        .fillMaxWidth()
                        .alpha(if (showAudioControls) 0.4f else 1.0f)
                )
                if (showAudioControls) {
                    val player = AudioProperties.mediaController.get()
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .height(100.dp)
                            .fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier)
                        Icon(
                            imageVector = if (AudioProperties.isPlaying)
                                Icons.Default.Pause
                            else
                                Icons.Default.PlayArrow,
                            contentDescription = "Pause/Play",
                            modifier = Modifier
                                .size(52.dp)
                                .clickable {
                                    if (AudioProperties.isPlaying)
                                        player.pause()
                                    else
                                        player.play()
                                }
                        )
                        LinearProgressIndicator(
                            progress = {
                                if (AudioProperties.audioLength == 0L) {
                                    0f
                                } else {
                                    AudioProperties.currentPosition.toFloat() / AudioProperties.audioLength
                                }
                            }
                        )
                    }
                }
            }
            Text(
                text = mediaInfo.name,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 3.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun MediaListItem(mediaInfo: MediaInfo, modifier: Modifier = Modifier) {
    val showAudioControls =
        mediaInfo.mediaType == MediaType.Audio && AudioProperties.compareAudioFile(File(mediaInfo.filePath))
    val modifierClickable = if (showAudioControls) {
        val context = LocalContext.current
        modifier.clickable { showAudioPlayer(context) }
    } else modifier
    Surface(
        modifier = modifierClickable
            .fillMaxWidth()
            .padding(4.dp)
            .animateContentSize(),
        shape = MaterialTheme.shapes.small,
        border = if (showAudioControls)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null,
        color = if (showAudioControls)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            mediaInfo.MediaIcon(
                modifier = Modifier
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
                Text(text = mediaInfo.name, style = MaterialTheme.typography.bodyLarge)
                if (showAudioControls) {
                    val player = AudioProperties.mediaController.get()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Slider(
                            value = if (AudioProperties.audioLength == 0L) {
                                0f
                            } else {
                                AudioProperties.currentPosition.toFloat() / AudioProperties.audioLength
                            },
                            onValueChange = {
                                player.seekTo((it * AudioProperties.audioLength).toLong())
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (AudioProperties.isPlaying)
                                Icons.Default.Pause
                            else
                                Icons.Default.PlayArrow,
                            contentDescription = "Pause/Play",
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    if (AudioProperties.isPlaying)
                                        player.pause()
                                    else
                                        player.play()
                                }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMilliseconds(mediaInfo.lastModified),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatFileSize(mediaInfo.size),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun IndicatorContent(indicatorString: String, isThumbSelected: Boolean) {
    if (!isThumbSelected)
        return
    Row {
        Text(
            text = indicatorString,
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(4.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun ShowAllMedia(
    viewBy: ViewBy,
    sortBy: SortBy,
    listState: LazyListState,
    gridState: LazyGridState,
    onClick: (MediaInfo) -> Unit,
    list: List<MediaInfo>,
    scrollDirectionListener: (Boolean) -> Unit = {},
    modifier: Modifier,
) {
    var isScrollThumbSelected by remember { mutableStateOf(false) }
    val getIndicatorString = { index: Int ->
        when (sortBy) {
            SortBy.Name -> "  ${list[index].name[0].uppercase()}  "
            SortBy.LastModified -> formatMilliseconds(list[index].lastModified, "dd-MMM-yyyy")
            SortBy.Size -> formatFileSize(list[index].size)
        }
    }
    when (viewBy) {
        ViewBy.List -> {
            LazyColumnScrollbar(
                listState = listState,
                thumbColor = MaterialTheme.colorScheme.primary,
                thumbSelectedColor = MaterialTheme.colorScheme.secondary,
                thickness = 8.dp,
                hideDelayMillis = 1500,
                indicatorContent = { index, isThumbSelected ->
                    IndicatorContent(
                        indicatorString = getIndicatorString(index),
                        isThumbSelected = isThumbSelected
                    )
                    isScrollThumbSelected = isThumbSelected
                }
            ) {
                LazyColumn(
                    modifier = modifier,
                    state = listState
                ) {
                    items(list) {
                        MediaListItem(it, Modifier.clickable { onClick(it) })
                    }
                }
            }
            scrollDirectionListener(isScrollThumbSelected || !listState.isScrollingUp())
        }

        ViewBy.Grid -> {
            LazyGridVerticalScrollbar(
                state = gridState,
                thumbColor = MaterialTheme.colorScheme.primary,
                thumbSelectedColor = MaterialTheme.colorScheme.secondary,
                thickness = 8.dp,
                hideDelayMillis = 1500,
                indicatorContent = { index, isThumbSelected ->
                    IndicatorContent(
                        indicatorString = getIndicatorString(index),
                        isThumbSelected = isThumbSelected
                    )
                    isScrollThumbSelected = isThumbSelected
                }
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(135.dp),
                    modifier = modifier,
                    state = gridState
                ) {
                    items(list) {
                        MediaGridItem(it, Modifier.clickable { onClick(it) })
                    }
                }
            }
            scrollDirectionListener(isScrollThumbSelected || !gridState.isScrollingUp())
        }
    }
}

private fun formatMilliseconds(milliseconds: Long, pattern: String = "dd MMM yyyy, HH:mm"): String {
    val date = Date(milliseconds)
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
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

@Composable
fun Modifier.simpleVerticalScrollbar(
    state: LazyListState, width: Dp = 4.dp,
): Modifier {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha, animationSpec = tween(durationMillis = duration), label = ""
    )

    return drawWithContent {
        drawContent()

        val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
        val needDrawScrollbar = state.isScrollInProgress || alpha > 0.0f

        // Draw scrollbar if scrolling or if the animation is still running and lazy column has content
        if (needDrawScrollbar && firstVisibleElementIndex != null) {
            val elementHeight = this.size.height / state.layoutInfo.totalItemsCount
            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
            var scrollbarHeight = state.layoutInfo.visibleItemsInfo.size * elementHeight
            if (scrollbarHeight < 10) scrollbarHeight = 10f
            drawRect(
                color = Color.Gray,
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                alpha = alpha
            )
        }
    }
}

@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                if (!isScrollInProgress) {
                    previousIndex = firstVisibleItemIndex
                    previousScrollOffset = firstVisibleItemScrollOffset
                }
            }
        }
    }.value
}

@Composable
private fun LazyGridState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                if (!isScrollInProgress) {
                    previousIndex = firstVisibleItemIndex
                    previousScrollOffset = firstVisibleItemScrollOffset
                }
            }
        }
    }.value
}

private fun addToRecent(context: Context, mediaType: MediaType, filePath: String) {
    val recentFile = File("${context.filesDir}/recent.map")
    if (!recentFile.exists()) {
        ObjectOutputStream(FileOutputStream(recentFile)).use {
            it.writeObject(mapOf(filePath to Pair(mediaType, System.currentTimeMillis())))
        }
        return
    }
    ObjectInputStream(FileInputStream(recentFile)).use { input ->
        val recentMap = (input.readObject() as Map<*, *>).toMutableMap()
        recentMap[filePath] = Pair(mediaType, System.currentTimeMillis())
        ObjectOutputStream(FileOutputStream(recentFile)).use {
            it.writeObject(recentMap)
        }
    }
}

fun showAudioPlayer(context: Context) {
    val intent = Intent(context, AudioPlayerActivity::class.java)
    context.startActivity(intent)
}

fun onMediaClick(
    context: Context,
    mediaType: MediaType,
    filePath: String,
) {
    addToRecent(context, mediaType, filePath)
    val intent = when (mediaType) {
        MediaType.Image -> Intent(context, ImageViewerActivity::class.java)
        MediaType.Audio -> {
            val mediaController = AudioProperties.mediaController.get()
            val audioFile = File(filePath)
            AudioProperties.currentlyPlayingFile = audioFile
            mediaController.setMediaItem(MediaItem.fromUri(Uri.fromFile(audioFile)))
            mediaController.seekTo(0L)
            mediaController.play()
            return
        }

        MediaType.Video -> Intent(context, VideoPlayerActivity::class.java)
        MediaType.PDF -> Intent(context, PDFViewerActivity::class.java)
    }
    intent.putExtra("path", filePath)
    context.startActivity(intent)
}