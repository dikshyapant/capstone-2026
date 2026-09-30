package com.teammonarch.butterfly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.teammonarch.butterfly.navigation.ButterflyNavGraph
import com.teammonarch.butterfly.ui.theme.ButterflyTheme
import com.teammonarch.butterfly.notifications.NotificationHelper
import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
    private val permissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        // Optional: react to the user's choice
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            ButterflyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ButterflyNavGraph()
                }
            }
        }
    }
}
