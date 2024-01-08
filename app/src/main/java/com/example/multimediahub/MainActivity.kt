package com.example.multimediahub

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.ContextThemeWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.multimediahub.audioplayer.AudioPlayerService
import com.example.multimediahub.audioplayer.AudioProperties
import com.example.multimediahub.screens.MainScreen
import com.example.multimediahub.ui.theme.MultimediaHubTheme
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    private var isPermissionGranted: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isPermissionGranted = permissionState()
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it != isPermissionGranted)
                recreate()
        }
        requestStoragePermissions()
        setContent {
            MultimediaHubTheme {
                MainScreen()
            }
        }
        if (isPermissionGranted) {
            AudioProperties.sessionToken =
                SessionToken(this, ComponentName(this, AudioPlayerService::class.java))
            AudioProperties.mediaController =
                MediaController.Builder(this, AudioProperties.sessionToken).buildAsync()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isPermissionGranted) {
            AudioProperties.mediaController.get().release()
            exitProcess(0)
        }
    }

    override fun onResume() {
        super.onResume()
        val newPermissionState = permissionState()
        if (isPermissionGranted != newPermissionState)
            recreate()
    }

    private fun permissionState() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermissions() {
        if (permissionState()) return
        AlertDialog.Builder(ContextThemeWrapper(this, R.style.DarkDialogTheme))
            .setTitle("Allow storage permissions")
            .setMessage("This application needs storage permissions to access media files.")
            .setPositiveButton("OK") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } else {
                    requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), 0)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }
}