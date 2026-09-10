package com.teammonarch.butterfly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.teammonarch.butterfly.data.ProfileRow
import com.teammonarch.butterfly.data.SupabaseRepository
import com.teammonarch.butterfly.ui.components.BrandLogo
import com.teammonarch.butterfly.ui.theme.AuthGradient
import com.teammonarch.butterfly.ui.theme.CardWhite
import com.teammonarch.butterfly.ui.theme.DeepViolet
import com.teammonarch.butterfly.ui.theme.MonarchRed
import kotlinx.coroutines.launch

private enum class SignUpRole(val dbValue: String) {
    PATIENT("patient"), CLINICIAN("clinician")
}

@Composable
fun SignUpScreen(
    onAccountCreated: (ProfileRow) -> Unit,
    onBackToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(SignUpRole.PATIENT) }
    var agreedToTerms by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun submit() {
        errorMessage = when {
            name.isBlank() || email.isBlank() || password.isBlank() -> "Fill in all fields."
            password != confirmPassword -> "Passwords don't match."
            password.length < 6 -> "Password must be at least 6 characters."
            else -> null
        }
        if (errorMessage != null) return

        isLoading = true
        scope.launch {
            val result = SupabaseRepository.signUp(
                email = email.trim(),
                password = password,
                fullName = name.trim(),
                role = role.dbValue
            )
            isLoading = false
            result.onSuccess { onAccountCreated(it) }
                .onFailure { errorMessage = it.message ?: "Sign up failed." }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthGradient)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        BrandLogo(tint = CardWhite)
        Spacer(Modifier.height(16.dp))
        Text(
            "Create Your Account",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = CardWhite
        )
        Spacer(Modifier.height(20.dp))

        val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CardWhite,
            unfocusedContainerColor = CardWhite,
            focusedBorderColor = DeepViolet,
            unfocusedBorderColor = CardWhite
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))
        Text(
            "I am a:",
            style = MaterialTheme.typography.titleSmall,
            color = CardWhite,
            modifier = Modifier.align(Alignment.Start)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.selectable(
                    selected = role == SignUpRole.PATIENT,
                    onClick = { role = SignUpRole.PATIENT }
                )
            ) {
                RadioButton(
                    selected = role == SignUpRole.PATIENT,
                    onClick = { role = SignUpRole.PATIENT },
                    colors = RadioButtonDefaults.colors(selectedColor = CardWhite, unselectedColor = CardWhite)
                )
                Text("Patient", color = CardWhite)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.selectable(
                    selected = role == SignUpRole.CLINICIAN,
                    onClick = { role = SignUpRole.CLINICIAN }
                )
            ) {
                RadioButton(
                    selected = role == SignUpRole.CLINICIAN,
                    onClick = { role = SignUpRole.CLINICIAN },
                    colors = RadioButtonDefaults.colors(selectedColor = CardWhite, unselectedColor = CardWhite)
                )
                Text("Clinician", color = CardWhite)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = agreedToTerms,
                onCheckedChange = { agreedToTerms = it },
                colors = CheckboxDefaults.colors(checkedColor = CardWhite, checkmarkColor = DeepViolet)
            )
            Text("I agree to the Terms & Privacy Policy", color = CardWhite, style = MaterialTheme.typography.bodySmall)
        }

        if (errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(errorMessage.orEmpty(), color = MonarchRed, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = ::submit,
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(containerColor = DeepViolet, contentColor = CardWhite),
            enabled = agreedToTerms && !isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.height(20.dp), color = CardWhite)
            } else {
                Text("Create Account")
            }
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onBackToLogin) {
            Text("Already have an account? Log In", color = CardWhite, fontWeight = FontWeight.Bold)
        }
    }
}
