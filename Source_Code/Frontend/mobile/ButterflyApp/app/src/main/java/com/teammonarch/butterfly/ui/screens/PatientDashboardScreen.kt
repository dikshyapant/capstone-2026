package com.teammonarch.butterfly.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teammonarch.butterfly.data.AppSession
import com.teammonarch.butterfly.data.MarkTakenResult
import com.teammonarch.butterfly.data.MedicationRow
import com.teammonarch.butterfly.data.SupabaseRepository
import com.teammonarch.butterfly.model.GameStage
import com.teammonarch.butterfly.ui.components.ButterflyBottomNav
import com.teammonarch.butterfly.ui.components.NavTab
import com.teammonarch.butterfly.ui.components.NotificationBell
import com.teammonarch.butterfly.ui.theme.CardWhite
import com.teammonarch.butterfly.ui.theme.DeepViolet
import com.teammonarch.butterfly.ui.theme.GoldAmber
import com.teammonarch.butterfly.ui.theme.HomeGradient
import com.teammonarch.butterfly.ui.theme.MonarchRed
import com.teammonarch.butterfly.ui.theme.TextMuted
import com.teammonarch.butterfly.ui.theme.Violet
import com.teammonarch.butterfly.ui.theme.butterflyColorFor
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PatientDashboardScreen(
    onNavigateToAccount: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToBank: () -> Unit,
    onNavigateToCustomize: () -> Unit,
    onNavigateToNotifications: () -> Unit = {}
) {
    val profile = AppSession.currentProfile
    var bb by remember { mutableStateOf(profile?.currentBb ?: 0) }
    var streak by remember { mutableStateOf(profile?.currentStreak ?: 0) }
    val reduceMotion = profile?.reduceMotion ?: false
    val accent = butterflyColorFor(profile?.butterflyColor ?: "gold")
    var medications by remember { mutableStateOf<List<MedicationRow>>(emptyList()) }
    var medStatuses by remember { mutableStateOf(mapOf<String, Boolean>()) } // medId -> wasOnTime
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastReward by remember { mutableStateOf<MarkTakenResult?>(null) }
    var showDoubleMonarchDialog by remember { mutableStateOf(false) }
    var lateBanner by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(NavTab.HOME) }
    var newMedName by remember { mutableStateOf("") }
    var newMedTime by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val stage = GameStage.forBB(bb)

    LaunchedEffect(profile?.id) {
        val patientId = profile?.id ?: return@LaunchedEffect
        val result = SupabaseRepository.fetchMedications(patientId)
        result.onSuccess { medications = it }
            .onFailure { errorMessage = it.message }
        SupabaseRepository.fetchTodaysMedicationStatuses(patientId)
            .onSuccess { medStatuses = it }
        SupabaseRepository.resetStreakIfMissedDay(patientId)
            .onSuccess { resetStreak ->
                if (resetStreak != streak) {
                    streak = resetStreak
                    AppSession.currentProfile = profile.copy(currentStreak = resetStreak)
                }
            }
        isLoading = false
    }

    fun markTaken(med: MedicationRow) {
        val patientId = profile?.id ?: return
        val medId = med.id ?: return
        scope.launch {
            val result = SupabaseRepository.markMedicationTaken(patientId, medId, med.scheduledTime)
            result.onSuccess { r ->
                bb = r.newBalance
                streak = r.newStreak
                medStatuses = medStatuses + (medId to r.wasOnTime)
                lastReward = r
                showDoubleMonarchDialog = r.isDoubleMonarchDay
                lateBanner = !r.wasOnTime
                AppSession.currentProfile = profile.copy(currentBb = r.newBalance, currentStreak = r.newStreak)
            }.onFailure { errorMessage = it.message }
        }
    }

    fun addMedication() {
        val patientId = profile?.id ?: return
        if (newMedName.isBlank() || newMedTime.isBlank()) return
        scope.launch {
            val result = SupabaseRepository.addMedication(patientId, newMedName.trim(), newMedTime.trim())
            result.onSuccess {
                newMedName = ""
                newMedTime = ""
                SupabaseRepository.fetchMedications(patientId).onSuccess { medications = it }
            }.onFailure { errorMessage = it.message }
        }
    }

    Scaffold(
        bottomBar = {
            ButterflyBottomNav(selected = selectedTab) { tab ->
                when (tab) {
                    NavTab.SETTINGS -> onNavigateToAccount()
                    NavTab.HISTORY -> onNavigateToHistory()
                    NavTab.BANK -> onNavigateToBank()
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
                            "Welcome back, ${profile?.fullName ?: "there"}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = DeepViolet,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .background(CardWhite, MaterialTheme.shapes.extraLarge)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("🫙 $bb BB", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = DeepViolet)
                        }
                        Spacer(Modifier.width(8.dp))
                        NotificationBell(
                            onClick = onNavigateToNotifications,
                            count = medications.count { medStatuses[it.id] == null }
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

                    val nextMed = medications.firstOrNull { medStatuses[it.id] == null } ?: medications.firstOrNull()
                    if (nextMed != null) {
                        Card(colors = CardDefaults.cardColors(containerColor = CardWhite), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Med Status", fontWeight = FontWeight.Bold)
                                        Text(
                                            "Scheduled dose: ${nextMed.scheduledTime}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                    }
                                    val onTime = medStatuses[nextMed.id]
                                    when (onTime) {
                                        true -> StatusChip("On Time", Color(0xFF4A9D6E))
                                        false -> StatusChip("Late", Color(0xFFE0A030))
                                        null -> if (isDueSoon(nextMed.scheduledTime)) {
                                            StatusChip("Due now", GoldAmber)
                                        } else {
                                            StatusChip("Upcoming", TextMuted)
                                        }
                                    }
                                }
                                if (medStatuses[nextMed.id] == null) {
                                    Spacer(Modifier.height(12.dp))
                                    Button(onClick = { markTaken(nextMed) }, modifier = Modifier.fillMaxWidth()) {
                                        Text("Log Dose")
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    if (lateBanner) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Logged as late (+1 BB) — outside the 1-hour window, so no halo or streak credit today.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                IconButton(onClick = { lateBanner = false }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Dismiss")
                                }
                            }
                        }
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = CardWhite), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HaloGlow(active = streak > 0, reduceMotion = reduceMotion, haloColor = accent, modifier = Modifier.size(56.dp)) {
                                Text("🦋", style = MaterialTheme.typography.titleLarge)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("HALO STREAK", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text(
                                    if (streak > 0) "$streak-day streak" else "No streak yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = accent
                                )
                                val anyLateToday = medStatuses.values.any { it == false }
                                Text(
                                    when {
                                        streak == 0 -> "Log a dose on time to start one"
                                        anyLateToday -> "Streak holds, but a dose was late today"
                                        else -> "All doses on time so far"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Card(colors = CardDefaults.cardColors(containerColor = CardWhite), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Butterfly Bucks", fontWeight = FontWeight.Bold)
                                Text("Current Balance: $bb BB", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                            }
                            Text("🫙", style = MaterialTheme.typography.displaySmall, color = accent)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Card(colors = CardDefaults.cardColors(containerColor = CardWhite), modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("${stage.label} Stage", fontWeight = FontWeight.Bold)
                            HaloGlow(active = streak > 0, reduceMotion = reduceMotion, haloColor = accent, modifier = Modifier.size(96.dp)) {
                                Text(stage.emoji, style = MaterialTheme.typography.displayMedium)
                            }
                            val nextStage = when (stage) {
                                GameStage.CHRYSALIS -> GameStage.CATERPILLAR
                                GameStage.CATERPILLAR -> GameStage.BUTTERFLY
                                GameStage.BUTTERFLY -> null
                            }
                            if (nextStage != null) {
                                val progress = (bb.toFloat() / nextStage.minBB.toFloat()).coerceIn(0f, 1f)
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = progress,
                                    color = Violet,
                                    modifier = Modifier.fillMaxWidth().height(8.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("${(progress * 100).toInt()}% to ${nextStage.label}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            } else {
                                Text("Max stage reached! 🎉", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        QuickActionButton("Butterfly Bank", Modifier.weight(1f), onClick = onNavigateToBank)
                        QuickActionButton("Adherence History", Modifier.weight(1f), onClick = onNavigateToHistory)
                        QuickActionButton("Customise Butterfly", Modifier.weight(1f), onClick = onNavigateToCustomize)
                    }
                }
            }

            item {
                Text("Today's Medications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
            }

            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (medications.isEmpty()) {
                item {
                    Text("No medications yet — add one below.", color = TextMuted, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }

            items(medications, key = { it.id ?: it.name }) { med ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    MedicationRowCard(
                        med = med,
                        wasOnTime = medStatuses[med.id],
                        onMarkTaken = { markTaken(med) }
                    )
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Add a Medication", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newMedName,
                            onValueChange = { newMedName = it },
                            label = { Text("Medication name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newMedTime,
                            onValueChange = { newMedTime = it },
                            label = { Text("Scheduled time (e.g. 8:00 AM)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = ::addMedication, modifier = Modifier.fillMaxWidth()) {
                            Text("Add Medication")
                        }
                    }
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

    if (showDoubleMonarchDialog && lastReward != null) {
        val reward = lastReward!!
        AlertDialog(
            onDismissRequest = { showDoubleMonarchDialog = false },
            title = { Text("✨ Double Monarch Day!", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("$streak-day streak · every dose on time")
                    Spacer(Modifier.height(12.dp))
                    DialogRow("Base award", "${reward.bbAwarded / 2} BB")
                    DialogRow("Multiplier", "× 2.0")
                    DialogRow("New balance", "${reward.newBalance} BB")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Butterfly Bucks have no real-world monetary value.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDoubleMonarchDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }
}

@Composable
private fun DialogRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HaloGlow(
    active: Boolean,
    reduceMotion: Boolean,
    haloColor: Color = GoldAmber,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "halo")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(animation = tween(1200), repeatMode = RepeatMode.Reverse),
        label = "haloAlpha"
    )
    val alpha = when {
        !active -> 0f
        reduceMotion -> 0.55f
        else -> animatedAlpha
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (active) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(listOf(haloColor.copy(alpha = alpha), Color.Transparent)),
                        shape = CircleShape
                    )
            )
        }
        content()
    }
}

@Composable
private fun QuickActionButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun MedicationRowCard(med: MedicationRow, wasOnTime: Boolean?, onMarkTaken: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = CardWhite), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(med.name, fontWeight = FontWeight.Bold)
                Text("Scheduled dose: ${med.scheduledTime}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            if (wasOnTime == true) {
                StatusChip("On Time", Color(0xFF4A9D6E))
            } else if (wasOnTime == false) {
                StatusChip("Late", Color(0xFFE0A030))
            } else {
                Button(onClick = onMarkTaken) { Text("Log Dose") }
            }
        }
    }
}

private val MED_STATUS_TIME_FORMATS = listOf(
    DateTimeFormatter.ofPattern("h:mm a", Locale.US),
    DateTimeFormatter.ofPattern("hh:mm a", Locale.US),
    DateTimeFormatter.ofPattern("H:mm", Locale.US)
)

/** True once a scheduled dose is within 30 minutes of now or already past -- otherwise it's just upcoming, not "due now". */
private fun isDueSoon(scheduledTimeText: String): Boolean {
    val cleaned = scheduledTimeText.trim().uppercase(Locale.US)
    val scheduledTime = MED_STATUS_TIME_FORMATS.firstNotNullOfOrNull {
        runCatching { LocalTime.parse(cleaned, it) }.getOrNull()
    } ?: return true
    return !LocalTime.now().isBefore(scheduledTime.minusMinutes(30))
}

@Composable
private fun StatusChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, color = color, style = MaterialTheme.typography.labelMedium)
    }
}