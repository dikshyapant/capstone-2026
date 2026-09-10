package com.teammonarch.butterfly.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teammonarch.butterfly.data.AppSession
import com.teammonarch.butterfly.data.SupabaseRepository
import com.teammonarch.butterfly.ui.theme.TextMuted
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    val profile = AppSession.currentProfile
    val isClinician = profile?.role == "clinician"
    var enabled by remember { mutableStateOf(profile?.notificationsEnabled ?: true) }
    val scope = rememberCoroutineScope()

    fun toggle(checked: Boolean) {
        val userId = profile?.id ?: return
        enabled = checked
        scope.launch {
            SupabaseRepository.updateNotificationsEnabled(userId, checked).onSuccess {
                AppSession.currentProfile = profile.copy(notificationsEnabled = checked)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isClinician) "Non-Adherence Alerts" else "Medication Reminders",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (isClinician)
                            "Get notified when a patient hasn't logged a dose."
                        else
                            "Get reminded when it's time to take your medication.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                Switch(checked = enabled, onCheckedChange = ::toggle)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "This saves your notification preference to your account. It doesn't control push delivery timing, which your team can wire up separately.",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}
