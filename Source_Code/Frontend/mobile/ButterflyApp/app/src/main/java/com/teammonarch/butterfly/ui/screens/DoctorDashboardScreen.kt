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
import androidx.compose.material3.IconButton
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
import com.teammonarch.butterfly.ui.components.NotificationBell
import com.teammonarch.butterfly.ui.theme.CardWhite
import com.teammonarch.butterfly.ui.theme.DeepViolet
import com.teammonarch.butterfly.ui.theme.GoldAmber
import com.teammonarch.butterfly.ui.theme.HomeGradient
import com.teammonarch.butterfly.ui.theme.MonarchRed
import com.teammonarch.butterfly.ui.theme.TextInk
import com.teammonarch.butterfly.ui.theme.TextMuted
import com.teammonarch.butterfly.ui.theme.Violet
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private fun timeBasedGreeting(): String {
    return when (LocalTime.now().hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

@Composable
fun DoctorDashboardScreen(
    onNavigateToAccount: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToNotifications: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(ClinicianNavTab.DASHBOARD) }
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${timeBasedGreeting()}, ${AppSession.currentProfile?.fullName ?: "Doctor"}!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = DeepViolet,
                            modifier = Modifier.weight(1f)
                        )
                        NotificationBell(
                            onClick = onNavigateToNotifications,
                            count = if (isLoading) 0 else nonAdherent.size
                        )
                        IconButton(onClick = onNavigateToAccount) {
                            Icon(
                                Icons.Filled.AccountCircle,
                                contentDescription = "Profile",
                                tint = DeepViolet
                            )
                        }
                    }
                    Text(
                        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                        style = MaterialTheme.typography.bodySmall,
                        color = DeepViolet.copy(alpha = 0.75f)
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
                                        val pending = nonAdherentCounts[patient.id] ?: 0
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(patient.fullName, style = MaterialTheme.typography.bodySmall)
                                            Text(
                                                "$pending dose${if (pending == 1) "" else "s"} pending",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MonarchRed
                                            )
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
                                if (brokeStreak.isNotEmpty()) {
                                    Spacer(Modifier.height(8.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(GoldAmber.copy(alpha = 0.3f), MaterialTheme.shapes.small)
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            "${brokeStreak.size} patient${if (brokeStreak.size == 1) "" else "s"} broke a streak this week",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextInk
                                        )
                                        Text(
                                            "Tap to view list",
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${patient.currentBb} BB", fontWeight = FontWeight.Bold, color = Violet)
                Spacer(Modifier.width(8.dp))
                StreakPill(patient.currentStreak)
            }
        }
    }
}

/** Read-only streak badge for the patient list, e.g. "14d" / "0d" (REQ-36). */
@Composable
private fun StreakPill(streakDays: Int) {
    Box(
        modifier = Modifier
            .background(GoldAmber.copy(alpha = 0.3f), MaterialTheme.shapes.small)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            "${streakDays}d",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TextInk
        )
    }
}