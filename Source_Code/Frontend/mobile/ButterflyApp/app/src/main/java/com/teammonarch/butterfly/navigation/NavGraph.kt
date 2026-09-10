package com.teammonarch.butterfly.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.teammonarch.butterfly.data.AppSession
import com.teammonarch.butterfly.ui.screens.AccountInfoScreen
import com.teammonarch.butterfly.ui.screens.AdherenceCalendarScreen
import com.teammonarch.butterfly.ui.screens.ButterflyBankScreen
import com.teammonarch.butterfly.ui.screens.ClinicianAlertsScreen
import com.teammonarch.butterfly.ui.screens.CustomiseButterflyScreen
import com.teammonarch.butterfly.ui.screens.DataPrivacyScreen
import com.teammonarch.butterfly.ui.screens.DoctorAccountScreen
import com.teammonarch.butterfly.ui.screens.DoctorDashboardScreen
import com.teammonarch.butterfly.ui.screens.HelpSupportScreen
import com.teammonarch.butterfly.ui.screens.LoginScreen
import com.teammonarch.butterfly.ui.screens.NotificationSettingsScreen
import com.teammonarch.butterfly.ui.screens.PatientAccountScreen
import com.teammonarch.butterfly.ui.screens.PatientDashboardScreen
import com.teammonarch.butterfly.ui.screens.SignUpScreen

object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val PATIENT_DASHBOARD = "patient_dashboard"
    const val PATIENT_ACCOUNT = "patient_account"
    const val ADHERENCE_HISTORY = "adherence_history"
    const val BUTTERFLY_BANK = "butterfly_bank"
    const val CUSTOMISE_BUTTERFLY = "customise_butterfly"
    const val DOCTOR_DASHBOARD = "doctor_dashboard"
    const val DOCTOR_ACCOUNT = "doctor_account"
    const val CLINICIAN_ALERTS = "clinician_alerts"
    const val ACCOUNT_INFO = "account_info"
    const val NOTIFICATION_SETTINGS = "notification_settings"
    const val DATA_PRIVACY = "data_privacy"
    const val HELP_SUPPORT = "help_support"
}

@Composable
fun ButterflyNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { profile ->
                    AppSession.currentProfile = profile
                    val destination = if (profile.role == "clinician") Routes.DOCTOR_DASHBOARD else Routes.PATIENT_DASHBOARD
                    navController.navigate(destination) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate(Routes.SIGN_UP) }
            )
        }
        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onAccountCreated = { profile ->
                    AppSession.currentProfile = profile
                    val destination = if (profile.role == "clinician") Routes.DOCTOR_DASHBOARD else Routes.PATIENT_DASHBOARD
                    navController.navigate(destination) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.PATIENT_DASHBOARD) {
            PatientDashboardScreen(
                onNavigateToAccount = { navController.navigate(Routes.PATIENT_ACCOUNT) },
                onNavigateToHistory = { navController.navigate(Routes.ADHERENCE_HISTORY) },
                onNavigateToBank = { navController.navigate(Routes.BUTTERFLY_BANK) },
                onNavigateToCustomize = { navController.navigate(Routes.CUSTOMISE_BUTTERFLY) }
            )
        }
        composable(Routes.ADHERENCE_HISTORY) {
            AdherenceCalendarScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.BUTTERFLY_BANK) {
            ButterflyBankScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.CUSTOMISE_BUTTERFLY) {
            CustomiseButterflyScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PATIENT_ACCOUNT) {
            PatientAccountScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                onNavigateToAccountInfo = { navController.navigate(Routes.ACCOUNT_INFO) },
                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATION_SETTINGS) },
                onNavigateToDataPrivacy = { navController.navigate(Routes.DATA_PRIVACY) },
                onNavigateToHelpSupport = { navController.navigate(Routes.HELP_SUPPORT) }
            )
        }
        composable(Routes.DOCTOR_DASHBOARD) {
            DoctorDashboardScreen(
                onNavigateToAccount = { navController.navigate(Routes.DOCTOR_ACCOUNT) },
                onNavigateToAlerts = { navController.navigate(Routes.CLINICIAN_ALERTS) }
            )
        }
        composable(Routes.CLINICIAN_ALERTS) {
            ClinicianAlertsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.DOCTOR_ACCOUNT) {
            DoctorAccountScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                onNavigateToAccountInfo = { navController.navigate(Routes.ACCOUNT_INFO) },
                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATION_SETTINGS) },
                onNavigateToDataPrivacy = { navController.navigate(Routes.DATA_PRIVACY) },
                onNavigateToHelpSupport = { navController.navigate(Routes.HELP_SUPPORT) }
            )
        }
        composable(Routes.ACCOUNT_INFO) {
            AccountInfoScreen(
                onBack = { navController.popBackStack() },
                onAccountDeleted = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.NOTIFICATION_SETTINGS) {
            NotificationSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.DATA_PRIVACY) {
            DataPrivacyScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.HELP_SUPPORT) {
            HelpSupportScreen(onBack = { navController.popBackStack() })
        }
    }
}
