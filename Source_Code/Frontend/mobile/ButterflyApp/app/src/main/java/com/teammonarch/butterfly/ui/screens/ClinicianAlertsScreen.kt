package com.teammonarch.butterfly.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
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
import com.teammonarch.butterfly.data.ProfileRow
import com.teammonarch.butterfly.data.SupabaseRepository
import com.teammonarch.butterfly.ui.theme.CardWhite
import com.teammonarch.butterfly.ui.theme.GoldAmber
import com.teammonarch.butterfly.ui.theme.MonarchRed
import com.teammonarch.butterfly.ui.theme.TextInk
import com.teammonarch.butterfly.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicianAlertsScreen(onBack: () -> Unit) {
    var patients by remember { mutableStateOf<List<ProfileRow>>(emptyList()) }
    var nonAdherentCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val result = SupabaseRepository.fetchAllPatients()
        isLoading = false
        result.onSuccess { patients = it }
            .onFailure { errorMessage = it.message }
        SupabaseRepository.fetchNonAdherentCounts()
            .onSuccess { nonAdherentCounts = it }
    }

    val nonAdherent = patients.filter { it.id in nonAdherentCounts.keys }
    val brokeStreak = SupabaseRepository.patientsWhoBrokeStreakThisWeek(patients)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Non-Adherence Alerts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (nonAdherent.isEmpty() && brokeStreak.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎉", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("All patients are on track today!", color = TextMuted)
                }
            } else {
                LazyColumn {
                    if (nonAdherent.isNotEmpty()) {
                        item {
                            Text(
                                "${nonAdherent.size} patient${if (nonAdherent.size == 1) "" else "s"} have a dose pending today",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        items(nonAdherent, key = { "missed_${it.id}" }) { patient ->
                            Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                AlertRow(patient, pending = nonAdherentCounts[patient.id] ?: 0)
                            }
                        }
                    }

                    if (brokeStreak.isNotEmpty()) {
                        item {
                            Text(
                                "${brokeStreak.size} patient${if (brokeStreak.size == 1) "" else "s"} broke a streak this week",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        items(brokeStreak, key = { "streak_${it.id}" }) { patient ->
                            Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                StreakAlertRow(patient)
                            }
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(errorMessage.orEmpty(), color = MonarchRed, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun AlertRow(patient: ProfileRow, pending: Int) {
    Card(colors = CardDefaults.cardColors(containerColor = CardWhite), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(patient.fullName, fontWeight = FontWeight.Bold)
                Text(
                    "$pending dose${if (pending == 1) "" else "s"} pending",
                    style = MaterialTheme.typography.labelSmall,
                    color = MonarchRed
                )
            }
            Icon(Icons.Filled.Warning, contentDescription = "Alert", tint = MonarchRed)
        }
    }
}

@Composable
private fun StreakAlertRow(patient: ProfileRow) {
    Card(colors = CardDefaults.cardColors(containerColor = GoldAmber.copy(alpha = 0.3f)), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(patient.fullName, fontWeight = FontWeight.Bold, color = TextInk)
                Text(
                    "Streak reset to 0",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
            Icon(Icons.Filled.Warning, contentDescription = "Streak broken", tint = TextInk)
        }
    }
}
