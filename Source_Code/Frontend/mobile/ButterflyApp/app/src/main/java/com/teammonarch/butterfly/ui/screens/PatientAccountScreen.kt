package com.teammonarch.butterfly.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teammonarch.butterfly.data.AppSession
import com.teammonarch.butterfly.data.SupabaseRepository
import com.teammonarch.butterfly.ui.components.ButterflyBottomNav
import com.teammonarch.butterfly.ui.components.NavTab
import com.teammonarch.butterfly.ui.theme.CardWhite
import com.teammonarch.butterfly.ui.theme.MonarchRed
import com.teammonarch.butterfly.ui.theme.TextMuted
import com.teammonarch.butterfly.ui.theme.Violet
import kotlinx.coroutines.launch

@Composable
fun PatientAccountScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    onNavigateToAccountInfo: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToDataPrivacy: () -> Unit,
    onNavigateToHelpSupport: () -> Unit
) {
    val profile = AppSession.currentProfile
    var reduceMotion by remember { mutableStateOf(profile?.reduceMotion ?: false) }
    var isLoggingOut by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun toggleReduceMotion(checked: Boolean) {
        val userId = profile?.id ?: return
        reduceMotion = checked
        scope.launch {
            SupabaseRepository.updateReduceMotion(userId, checked).onSuccess {
                AppSession.currentProfile = profile.copy(reduceMotion = checked)
            }
        }
    }

    fun logout() {
        isLoggingOut = true
        scope.launch {
            SupabaseRepository.signOut()
            isLoggingOut = false
            onLogout()
        }
    }

    Scaffold(
        bottomBar = {
            ButterflyBottomNav(selected = NavTab.SETTINGS) { tab ->
                if (tab == NavTab.HOME) onBack()
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = Violet, modifier = Modifier.height(80.dp))
            Text(profile?.fullName ?: "", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(profile?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = TextMuted)

            Spacer(Modifier.height(24.dp))

            AccountMenuItem(Icons.Filled.Person, "Account Information", onNavigateToAccountInfo)
            AccountMenuItem(Icons.Filled.Notifications, "Notification Settings", onNavigateToNotifications)
            AccountMenuItem(Icons.Filled.Lock, "Data & Privacy", onNavigateToDataPrivacy)
            AccountMenuItem(Icons.Filled.Help, "Help & Support", onNavigateToHelpSupport)

            Spacer(Modifier.height(16.dp))

            Card(colors = CardDefaults.cardColors(containerColor = CardWhite), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reduce Motion", fontWeight = FontWeight.Bold)
                        Text(
                            "Show a steady glow instead of an animated one for adherence halos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    Switch(checked = reduceMotion, onCheckedChange = ::toggleReduceMotion)
                }
            }

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = ::logout,
                enabled = !isLoggingOut,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MonarchRed),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (isLoggingOut) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp))
                } else {
                    Text("Log Out")
                }
            }
        }
    }
}

@Composable
private fun AccountMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Violet)
                Spacer(Modifier.width(12.dp))
                Text(label)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}
