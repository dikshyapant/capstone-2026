package com.teammonarch.butterfly.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.teammonarch.butterfly.data.ProfileRow
import com.teammonarch.butterfly.data.SupabaseRepository
import com.teammonarch.butterfly.ui.components.ClinicianBottomNav
import com.teammonarch.butterfly.ui.components.ClinicianNavTab
import com.teammonarch.butterfly.ui.theme.CardWhite
import com.teammonarch.butterfly.ui.theme.DeepViolet
import com.teammonarch.butterfly.ui.theme.HomeGradient
import com.teammonarch.butterfly.ui.theme.MonarchRed
import com.teammonarch.butterfly.ui.theme.TextMuted
import com.teammonarch.butterfly.ui.theme.Violet
import java.time.LocalDate

@Composable
fun DoctorDashboardScreen(onNavigateToAccount: () -> Unit, onNavigateToAlerts: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(ClinicianNavTab.DASHBOARD) }
    var patients by remember { mutableStateOf<List<ProfileRow>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val result = SupabaseRepository.fetchAllPatients()
        isLoading = false
        result.onSuccess { patients = it }
            .onFailure { errorMessage = it.message }
    }

    val today = LocalDate.now().toString()
    val nonAdherent = patients.filter { it.lastLogDate != today }
    val filtered = patients.filter { it.fullName.contains(query, ignoreCase = true) }

    Scaffold(
        bottomBar = {
            ClinicianBottomNav(selected = selectedTab) { tab ->
                when (tab) {
                    ClinicianNavTab.SETTINGS -> onNavigateToAccount()
                    ClinicianNavTab.ALERTS -> onNavigateToAlerts()
                    else -> selectedTab = tab
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HomeGradient)
                        .padding(20.dp)
                ) {
                    Text(
                        "Good Morning, ${AppSession.currentProfile?.fullName ?: "Doctor"}!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepViolet
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Search patients...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!isLoading) {
                        Spacer(Modifier.height(16.dp))
                        Card(
                            onClick = onNavigateToAlerts,
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Non-Adherence Alerts", fontWeight = FontWeight.Bold)
                                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted)
                                }
                                Spacer(Modifier.height(8.dp))
                                if (nonAdherent.isEmpty()) {
                                    Text("All patients are on track today.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                } else {
                                    nonAdherent.take(2).forEach { patient ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(patient.fullName, style = MaterialTheme.typography.bodySmall)
                                            Text("No dose today", style = MaterialTheme.typography.labelSmall, color = MonarchRed)
                                        }
                                    }
                                    if (nonAdherent.size > 2) {
                                        Text(
                                            "+ ${nonAdherent.size - 2} more",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "All Patients",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (filtered.isEmpty()) {
                item {
                    Text(
                        "No patients have signed up yet.",
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            items(filtered, key = { it.id }) { patient ->
                Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    PatientRow(patient)
                }
            }

            if (errorMessage != null) {
                item {
                    Text(errorMessage.orEmpty(), color = MonarchRed, modifier = Modifier.padding(16.dp))
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun PatientRow(patient: ProfileRow) {
    Card(colors = CardDefaults.cardColors(containerColor = CardWhite), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = Violet)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(patient.fullName, fontWeight = FontWeight.Bold)
                    Text(patient.email, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }
            Text("${patient.currentBb} BB", fontWeight = FontWeight.Bold, color = Violet)
        }
    }
}
