package com.example.multimediahub.imageviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.multimediahub.screens.MessageText
import me.saket.telephoto.zoomable.glide.ZoomableGlideImage
import java.io.File

class ImageViewerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val path = intent.extras?.getString("path")
        setContent {
            if (path == null) {
                MessageText("Failed to load image.")
            } else {
                Content(path)
            }
        }
    }

    @Composable
    private fun Content(path: String) {
        Surface(color = Color.Black) {
            Column {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { finish() }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = File(path).name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                ZoomableGlideImage(
                    model = "file://$path",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

