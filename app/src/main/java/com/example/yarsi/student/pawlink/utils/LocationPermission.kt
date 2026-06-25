package com.example.yarsi.student.pawlink.utils

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

// State permission lokasi
data class LocationPermissionState(
    val isGranted: Boolean,
    val requestPermission: () -> Unit
)

@Composable
fun rememberLocationPermissionState(
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {}
): LocationPermissionState {
    val context = LocalContext.current
    var isGranted by remember {
        mutableStateOf(LocationHelper.isLocationPermissionGranted(context))
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        isGranted = granted
        if (granted) onGranted() else onDenied()
    }

    return LocationPermissionState(
        isGranted = isGranted,
        requestPermission = {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    )
}