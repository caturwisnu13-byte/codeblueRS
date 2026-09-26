package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CodeBlueApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CodeBlueViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: CodeBlueViewModel = viewModel()

                // Request Notification Permission on Android 13+
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // Notification permission handled
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    // Check if opened via intent
                    val targetAlertId = intent?.getStringExtra("ALERT_ID")
                    if (targetAlertId != null) {
                        viewModel.selectAlert(targetAlertId)
                        viewModel.switchRole(com.example.viewmodel.AppRole.CODE_BLUE_TEAM)
                    }

                    // Check if opened via deep link / room account link
                    val dataUri = intent?.data
                    if (dataUri != null) {
                        viewModel.bindDeviceToLink(dataUri.toString())
                        viewModel.switchRole(com.example.viewmodel.AppRole.ROOM_REQUESTER)
                    }
                }

                CodeBlueApp(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
