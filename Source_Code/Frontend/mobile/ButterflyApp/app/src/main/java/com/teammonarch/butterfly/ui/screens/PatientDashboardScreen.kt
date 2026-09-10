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
import androidx.compose.material.icons.Icons
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
import com.teammonarch.butterfly.data.MedicationRow
import com.teammonarch.butterfly.data.SupabaseRepository
import com.teammonarch.butterfly.model.GameStage
import com.teammonarch.butterfly.ui.components.ButterflyBottomNav
import com.teammonarch.butterfly.ui.components.NavTab
import com.teammonarch.butterfly.ui.theme.CardWhite
import com.teammonarch.butterfly.ui.theme.DeepViolet
import com.teammonarch.butterfly.ui.theme.GoldAmber
import com.teammonarch.butterfly.ui.theme.HomeGradient
import com.teammonarch.butterfly.ui.theme.MonarchRed
import com.teammonarch.butterfly.ui.theme.TextMuted
import com.teammonarch.butterfly.ui.theme.Violet
import com.teammonarch.butterfly.ui.theme.butterflyColorFor
import kotlinx.coroutines.launch

@Composable
fun PatientDashboardScreen(
    onNavigateToAccount: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToBank: () -> Unit,
    onNavigateToCustomize: () -> Unit
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
    var doubleMonarchBanner by remember { mutableStateOf(false) }
    var lateBanner by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(NavTab.HOME) }
    var newMedName by remember { mutableStateOf("") }
    var newMedTime by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val stage = GameStage.forBB(bb)

    LaunchedEffect(profile?.id) {
        val patientId = profile?.id ?: return@LaunchedEffect
        val result = SupabaseRepository.fetchMedications(patientId)
        isLoading = false
        result.onSuccess { medications = it }
            .onFailure { errorMessage = it.message }
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
                doubleMonarchBanner = r.isDoubleMonarchDay
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
                    Text(
                        "Good Morning, ${profile?.fullName ?: "there"}!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepViolet
                    )
                    Spacer(Modifier.height(16.dp))

                    if (doubleMonarchBanner) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GoldAmber.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✨ Double Monarch Day! +4 Butterfly Bucks", fontWeight = FontWeight.Bold)
                                IconButton(onClick = { doubleMonarchBanner = false }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Dismiss")
                                }
                            }
                        }
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Butterfly Bucks", fontWeight = FontWeight.Bold)
                                Text("Current Balance: $bb BB", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                                if (streak > 0) {
                                    Text("🔥 $streak-day streak", style = MaterialTheme.typography.labelMedium, color = accent)
                                }
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
                Text("Scheduled: ${med.scheduledTime}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            if (wasOnTime == true) {
                StatusChip("On Time", Color(0xFF4A9D6E))
            } else if (wasOnTime == false) {
                StatusChip("Late", Color(0xFFE0A030))
            } else {
                Button(onClick = onMarkTaken) { Text("Mark Taken") }
            }
        }
    }
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
