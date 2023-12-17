package com.example.multimediahub.screens

import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
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
import com.example.multimediahub.getMediaList

@Composable
fun FilesScreen(filesDisplayInfo: FilesDisplayInfo, modifier: Modifier = Modifier) {
    if (!checkStoragePermissionAndShowMessage())
        return
    var isLoaded by remember { mutableStateOf(false) }
    var fileList by remember { mutableStateOf(listOf<MediaInfo>()) }
    Log.d("MyAppTag", "IsLoaded: $isLoaded")
    val context = LocalContext.current
    LaunchedEffect(key1 = listOf(filesDisplayInfo.selectedMediaType, filesDisplayInfo.sortBy)) {
        fileList =
            getMediaList(context, filesDisplayInfo.selectedMediaType, filesDisplayInfo.sortBy)
        isLoaded = true
    }
    if (!isLoaded) {
        MessageText(msg = "Loading ...")
        return
    }
    LazyColumn(modifier = modifier) {
        items(fileList) {
            Text(it.name)
        }
    }
}