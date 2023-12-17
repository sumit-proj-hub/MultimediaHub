package com.example.multimediahub.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.multimediahub.FilesDisplayInfo

@Composable
fun RecentScreen(filesDisplayInfo: FilesDisplayInfo, modifier: Modifier = Modifier) {
    if (!checkStoragePermissionAndShowMessage())
        return
    Text(text = "Recent Screen")
}