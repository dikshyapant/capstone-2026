package com.teammonarch.butterfly.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teammonarch.butterfly.ui.theme.CardWhite
import com.teammonarch.butterfly.ui.theme.TextMuted

private data class PrivacyPoint(val title: String, val body: String)

private val PRIVACY_POINTS = listOf(
    PrivacyPoint(
        "Encrypted in transit",
        "All data sent between the app and our servers is encrypted over HTTPS."
    ),
    PrivacyPoint(
        "Secure storage",
        "Your medication and account data is stored using our hosting provider's secure database infrastructure."
    ),
    PrivacyPoint(
        "Role-based access",
        "Clinicians can only see adherence data for patients, never your login credentials or password."
    ),
    PrivacyPoint(
        "You're in control",
        "You can update or remove your information at any time from Account Info."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataPrivacyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data & Privacy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                "How we handle your data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "The Butterfly Project is a capstone prototype, but we treat health data seriously.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Spacer(Modifier.height(16.dp))

            PRIVACY_POINTS.forEach { point ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(point.title, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(point.body, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            }
        }
    }
}
