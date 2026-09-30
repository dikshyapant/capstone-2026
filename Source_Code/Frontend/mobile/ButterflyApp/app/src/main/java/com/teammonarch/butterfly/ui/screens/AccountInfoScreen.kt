package com.teammonarch.butterfly.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.teammonarch.butterfly.data.AppSession
import com.teammonarch.butterfly.data.SupabaseRepository
import com.teammonarch.butterfly.ui.theme.MonarchRed
import com.teammonarch.butterfly.ui.theme.TextMuted
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountInfoScreen(onBack: () -> Unit, onAccountDeleted: () -> Unit) {
    val profile = AppSession.currentProfile
    var name by remember { mutableStateOf(profile?.fullName ?: "") }
    var email by remember { mutableStateOf(profile?.email ?: "") }
    var phone by remember { mutableStateOf(profile?.phone ?: "") }
    var dob by remember { mutableStateOf(profile?.dateOfBirth ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun save() {
        val userId = profile?.id ?: return
        isSaving = true
        scope.launch {
            val result = SupabaseRepository.updateProfile(userId, name.trim(), email.trim(), phone.trim(), dob)
            isSaving = false
            result.onSuccess {
                AppSession.currentProfile = profile.copy(
                    fullName = name.trim(),
                    email = email.trim(),
                    phone = phone.trim(),
                    dateOfBirth = dob
                )
                message = "Saved."
            }.onFailure { message = it.message ?: "Couldn't save changes." }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Info") },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            // Tap anywhere on the field to open the calendar
            Box {
                OutlinedTextField(
                    value = dob,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date of Birth") },
                    placeholder = { Text("Tap to choose a date") },
                    trailingIcon = {
                        Icon(Icons.Filled.DateRange, contentDescription = "Choose date")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }

            if (message != null) {
                Spacer(Modifier.height(8.dp))
                Text(message.orEmpty(), style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = ::save,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.height(20.dp)) else Text("Save Changes")
            }

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = { showChangePassword = true },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("Change Password") }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MonarchRed),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("Delete Account") }
        }
    }

    if (showDatePicker) {
        val initialMillis = runCatching {
            LocalDate.parse(dob).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }.getOrNull()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            yearRange = 1900..LocalDate.now().year
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        dob = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                            .toString() // yyyy-MM-dd
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showChangePassword) {
        ChangePasswordDialog(
            onDismiss = { showChangePassword = false },
            onDone = { showChangePassword = false }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete account?") },
            text = { Text("This deactivates your account and signs you out. You won't be able to log back in. This can't be undone from the app.") },
            confirmButton = {
                TextButton(onClick = {
                    val userId = profile?.id
                    showDeleteConfirm = false
                    if (userId != null) {
                        scope.launch {
                            SupabaseRepository.deactivateAccount(userId)
                            onAccountDeleted()
                        }
                    }
                }) { Text("Delete", color = MonarchRed) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ChangePasswordDialog(onDismiss: () -> Unit, onDone: () -> Unit) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error.orEmpty(), color = MonarchRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    if (newPassword.length < 6) {
                        error = "Password must be at least 6 characters."
                        return@TextButton
                    }
                    if (newPassword != confirmPassword) {
                        error = "Passwords don't match."
                        return@TextButton
                    }
                    isSaving = true
                    scope.launch {
                        val result = SupabaseRepository.changePassword(newPassword)
                        isSaving = false
                        result.onSuccess { onDone() }
                            .onFailure { error = it.message ?: "Couldn't change password." }
                    }
                }
            ) { Text("Change") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}