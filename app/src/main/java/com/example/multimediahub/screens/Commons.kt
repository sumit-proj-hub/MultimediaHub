package com.example.multimediahub.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.multimediahub.MediaInfo
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
    Surface(
        modifier = modifier.padding(6.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column {
            mediaInfo.MediaIcon(
                Modifier
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
fun MediaListItem(mediaInfo: MediaInfo, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
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

@Composable
fun Modifier.simpleVerticalScrollbar(
    state: LazyListState,
    width: Dp = 4.dp
): Modifier {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration), label = ""
    )

    return drawWithContent {
        drawContent()

        val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
        val needDrawScrollbar = state.isScrollInProgress || alpha > 0.0f

        // Draw scrollbar if scrolling or if the animation is still running and lazy column has content
        if (needDrawScrollbar && firstVisibleElementIndex != null) {
            val elementHeight = this.size.height / state.layoutInfo.totalItemsCount
            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
            val scrollbarHeight = state.layoutInfo.visibleItemsInfo.size * elementHeight

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
fun Modifier.simpleVerticalScrollbar(
    state: LazyGridState,
    width: Dp = 4.dp
): Modifier {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration), label = ""
    )

    return drawWithContent {
        drawContent()

        val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
        val needDrawScrollbar = state.isScrollInProgress || alpha > 0.0f

        // Draw scrollbar if scrolling or if the animation is still running and lazy column has content
        if (needDrawScrollbar && firstVisibleElementIndex != null) {
            val elementHeight = this.size.height / state.layoutInfo.totalItemsCount
            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
            val scrollbarHeight = state.layoutInfo.visibleItemsInfo.size * elementHeight

            drawRect(
                color = Color.Gray,
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                alpha = alpha
            )
        }
    }
}