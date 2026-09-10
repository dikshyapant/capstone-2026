package com.teammonarch.butterfly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
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

@Composable
fun LoginScreen(
    onLoginSuccess: (ProfileRow) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun submit() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Enter your email and password."
            return
        }
        errorMessage = null
        isLoading = true
        scope.launch {
            val result = SupabaseRepository.signIn(email.trim(), password)
            isLoading = false
            result.onSuccess { onLoginSuccess(it) }
                .onFailure { errorMessage = it.message ?: "Login failed. Check your credentials." }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthGradient)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(24.dp))
        BrandLogo(tint = CardWhite)
        Spacer(Modifier.height(4.dp))
        Text(
            "Medication adherence, made rewarding",
            style = MaterialTheme.typography.bodyMedium,
            color = CardWhite.copy(alpha = 0.85f)
        )

        Spacer(Modifier.height(32.dp))

        val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CardWhite,
            unfocusedContainerColor = CardWhite,
            focusedBorderColor = DeepViolet,
            unfocusedBorderColor = CardWhite
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(errorMessage.orEmpty(), color = MonarchRed, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = ::submit,
            enabled = !isLoading,
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(containerColor = DeepViolet, contentColor = CardWhite),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.height(20.dp), color = CardWhite)
            } else {
                Text("Log In")
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onNavigateToSignUp) {
            Text("Don't have an account? Sign up", color = CardWhite, fontWeight = FontWeight.Bold)
        }
    }
}
