package com.teammonarch.butterfly.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.teammonarch.butterfly.ui.theme.CardWhite
import com.teammonarch.butterfly.ui.theme.DeepViolet
import com.teammonarch.butterfly.ui.theme.VioletLight

enum class NavTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    BANK("Bank", Icons.Filled.DateRange),
    HISTORY("History", Icons.Filled.AccountBox),
    SETTINGS("Settings", Icons.Filled.Settings)
}

@Composable
fun ButterflyBottomNav(
    selected: NavTab,
    onSelect: (NavTab) -> Unit
) {
    NavigationBar(containerColor = DeepViolet, contentColor = CardWhite) {
        NavTab.values().forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CardWhite,
                    selectedTextColor = CardWhite,
                    indicatorColor = VioletLight,
                    unselectedIconColor = CardWhite.copy(alpha = 0.6f),
                    unselectedTextColor = CardWhite.copy(alpha = 0.6f)
                )
            )
        }
    }
}

enum class ClinicianNavTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Filled.Home),
    PATIENTS("Patients", Icons.Filled.Person),
    ALERTS("Alerts", Icons.Filled.Warning),
    SETTINGS("Settings", Icons.Filled.Settings)
}

@Composable
fun ClinicianBottomNav(
    selected: ClinicianNavTab,
    onSelect: (ClinicianNavTab) -> Unit
) {
    NavigationBar(containerColor = DeepViolet, contentColor = CardWhite) {
        ClinicianNavTab.values().forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CardWhite,
                    selectedTextColor = CardWhite,
                    indicatorColor = VioletLight,
                    unselectedIconColor = CardWhite.copy(alpha = 0.6f),
                    unselectedTextColor = CardWhite.copy(alpha = 0.6f)
                )
            )
        }
    }
}
