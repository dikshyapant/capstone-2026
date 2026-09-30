package com.teammonarch.butterfly.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teammonarch.butterfly.data.AppSession
import com.teammonarch.butterfly.data.SupabaseRepository
import com.teammonarch.butterfly.ui.theme.CardWhite
import com.teammonarch.butterfly.ui.theme.MonarchRed
import com.teammonarch.butterfly.ui.theme.TextMuted
import com.teammonarch.butterfly.ui.theme.Violet
import java.time.LocalDate

private data class NotificationItem(
    val title: String,
    val subtitle: String,
    val needsAttention: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val profile = AppSession.currentProfile
    val isClinician = profile?.role == "clinician"
    var items by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(profile?.id) {
        if (isClinician) {
            val today = LocalDate.now().toString()
            SupabaseRepository.fetchAllPatients()
                .onSuccess { patients ->
                    items = patients
                        .filter { it.lastLogDate != today }
                        .map {
                            NotificationItem(
                                title = "${it.fullName} hasn't logged a dose today",
                                subtitle = "Non-adherence alert",
                                needsAttention = true
                            )
                        }
                }
                .onFailure { errorMessage = it.message }
        } else {
            val patientId = profile?.id
            if (patientId != null) {
                val statuses = SupabaseRepository.fetchTodaysMedicationStatuses(patientId)
                    .getOrDefault(emptyMap())
                SupabaseRepository.fetchMedications(patientId)
                    .onSuccess { meds ->
                        items = meds.map { med ->
                            when (statuses[med.id]) {
                                null -> NotificationItem(
                                    title = "Time to take ${med.name}",
                                    subtitle = "Scheduled for ${med.scheduledTime}",
                                    needsAttention = true
                                )
                                true -> NotificationItem(
                                    title = "${med.name} logged on time",
                                    subtitle = "Scheduled for ${med.scheduledTime}",
                                    needsAttention = false
                                )
                                false -> NotificationItem(
                                    title = "${med.name} logged late",
                                    subtitle = "Scheduled for ${med.scheduledTime}",
                                    needsAttention = false
                                )
                            }
                        }.sortedByDescending { it.needsAttention }
                    }
                    .onFailure { errorMessage = it.message }
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Notification settings")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            errorMessage != null -> Text(
                errorMessage.orEmpty(),
                color = MonarchRed,
                modifier = Modifier.padding(padding).padding(20.dp)
            )

            items.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isClinician) "All patients are on track today." else "No notifications yet.",
                    color = TextMuted
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items) { item ->
                    NotificationCard(item)
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(item: NotificationItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (item.needsAttention) Icons.Filled.Notifications else Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = if (item.needsAttention) MonarchRed else Violet
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(item.title, fontWeight = FontWeight.Bold)
                Text(
                    item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}