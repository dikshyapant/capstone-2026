package com.teammonarch.butterfly.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teammonarch.butterfly.data.AppSession
import com.teammonarch.butterfly.data.SupabaseRepository
import com.teammonarch.butterfly.ui.theme.CardWhite
import com.teammonarch.butterfly.ui.theme.TextMuted
import com.teammonarch.butterfly.ui.theme.butterflyColorFor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityScreen(onBack: () -> Unit) {
    val profile = AppSession.currentProfile
    var reduceMotion by remember { mutableStateOf(profile?.reduceMotion ?: false) }
    val haloAnimationOn = !reduceMotion
    val streak = profile?.currentStreak ?: 0
    val accent = butterflyColorFor(profile?.butterflyColor ?: "gold")
    val scope = rememberCoroutineScope()

    fun setHaloAnimation(on: Boolean) {
        val userId = profile?.id ?: return
        reduceMotion = !on
        scope.launch {
            SupabaseRepository.updateReduceMotion(userId, !on).onSuccess {
                AppSession.currentProfile = profile.copy(reduceMotion = !on)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accessibility") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            Text("Visual Effects", style = MaterialTheme.typography.labelMedium, color = TextMuted, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            Card(colors = CardDefaults.cardColors(containerColor = CardWhite), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Halo animation", fontWeight = FontWeight.Bold)
                        Text(
                            "Animates the halo while your streak is active.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    Switch(checked = haloAnimationOn, onCheckedChange = ::setHaloAnimation)
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(colors = CardDefaults.cardColors(containerColor = CardWhite.copy(alpha = 0.6f)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Preview", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PreviewHalo(active = streak > 0, reduceMotion = reduceMotion, haloColor = accent, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                if (streak > 0) "$streak-day streak" else "No streak yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Count and rewards still active", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(colors = CardDefaults.cardColors(containerColor = CardWhite), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("What stays on", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    WhatStaysOnRow("Your numeric streak count")
                    WhatStaysOnRow("Double Monarch Day 2× bonus")
                    WhatStaysOnRow("Doctor visibility of your streak")
                }
            }
        }
    }
}

@Composable
private fun WhatStaysOnRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF4A9D6E), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PreviewHalo(
    active: Boolean,
    reduceMotion: Boolean,
    haloColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "previewHalo")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(animation = tween(1200), repeatMode = RepeatMode.Reverse),
        label = "previewHaloAlpha"
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
        Text("🦋", style = MaterialTheme.typography.titleMedium)
    }
}
