package com.example.multimediahub

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.multimediahub.screens.MainScreen
import com.example.multimediahub.ui.theme.MultimediaHubTheme

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(
                    this,
                    "Please allow storage permissions to access files.",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        } else {
            if (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(
                        this,
                        "Please allow storage permissions to access files.",
                        Toast.LENGTH_LONG
                    ).show()
                    requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), 0)
                } else {
                    requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), 0)
                }
            }
        }
    }
}