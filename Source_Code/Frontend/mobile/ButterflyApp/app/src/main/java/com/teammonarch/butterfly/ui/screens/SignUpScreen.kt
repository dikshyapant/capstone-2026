package com.teammonarch.butterfly.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.ui.draw.alpha

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private enum class SignUpRole(val dbValue: String) {
    PATIENT("patient"), CLINICIAN("clinician")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onAccountCreated: (ProfileRow) -> Unit,
    onBackToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
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
                role = role.dbValue,
                dateOfBirth = dateOfBirth
            )
            isLoading = false
            result.onSuccess { onAccountCreated(it) }
                .onFailure { errorMessage = it.message ?: "Sign up failed." }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthGradient)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
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
                unfocusedBorderColor = CardWhite,

                focusedTextColor = DeepViolet,
                unfocusedTextColor = DeepViolet,

                focusedLabelColor = DeepViolet,
                unfocusedLabelColor = DeepViolet,

                cursorColor = DeepViolet
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
            Spacer(Modifier.height(12.dp))

            Box {
                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date of Birth") },
                    placeholder = { Text("Tap to choose a date") },
                    trailingIcon = {
                        Icon(Icons.Filled.DateRange, contentDescription = "Choose date")
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }

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
                        colors = RadioButtonDefaults.colors(
                            selectedColor = CardWhite,
                            unselectedColor = CardWhite
                        )
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
                        colors = RadioButtonDefaults.colors(
                            selectedColor = CardWhite,
                            unselectedColor = CardWhite
                        )
                    )
                    Text("Clinician", color = CardWhite)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = agreedToTerms,
                    onCheckedChange = { agreedToTerms = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = CardWhite,
                        checkmarkColor = DeepViolet
                    )
                )
                Text(
                    "I agree to the Terms & Privacy Policy",
                    color = CardWhite,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    errorMessage.orEmpty(),
                    color = MonarchRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = ::submit,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepViolet,
                    contentColor = CardWhite
                ),
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
                Text(
                    "Already have an account? Log In",
                    color = CardWhite,
                    fontWeight = FontWeight.Bold
                )
            }

        } // closes Column

        ButterflyEffectsOverlay()

    } // closes Box

    if (showDatePicker) {
        val initialMillis = runCatching {
            LocalDate.parse(dateOfBirth).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
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
                        dateOfBirth = Instant.ofEpochMilli(millis)
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
}
@Composable
fun ButterflyEffectsOverlay() {
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.toFloat()
    val screenHeight = config.screenHeightDp.toFloat()

    // 14 butterflies for the opening burst
    val burstButterflies = remember {
        List(14) { index ->

            val startX: Float
            val startY: Float

            // Spread starting positions around all four edges
            when (index % 4) {
                0 -> {
                    startX = -30f
                    startY = Random.nextFloat() * screenHeight
                }

                1 -> {
                    startX = screenWidth + 30f
                    startY = Random.nextFloat() * screenHeight
                }

                2 -> {
                    startX = Random.nextFloat() * screenWidth
                    startY = -30f
                }

                else -> {
                    startX = Random.nextFloat() * screenWidth
                    startY = screenHeight + 30f
                }
            }

            Triple(
                Animatable(startX), // X position
                Animatable(startY), // Y position
                Animatable(0f)      // visibility / alpha
            )
        }
    }

    LaunchedEffect(Unit) {

        burstButterflies.forEachIndexed { index, butterfly ->

            launch {

                val x = butterfly.first
                val y = butterfly.second
                val alpha = butterfly.third

                // Each butterfly enters a little after the previous one
                delay(index * 180L)

                // Fade IN
                launch {
                    alpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = LinearEasing
                        )
                    )
                }

                // Fly somewhere into the screen
                launch {
                    x.animateTo(
                        targetValue =
                            25f + Random.nextFloat() * (screenWidth - 50f),
                        animationSpec = tween(
                            durationMillis = 1800,
                            easing = FastOutSlowInEasing
                        )
                    )
                }

                launch {
                    y.animateTo(
                        targetValue =
                            40f + Random.nextFloat() * (screenHeight * 0.70f),
                        animationSpec = tween(
                            durationMillis = 1800,
                            easing = FastOutSlowInEasing
                        )
                    )
                }

                // Let this butterfly stay visible
                delay(2600L)

                // Slowly fade OUT
                alpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 2200,
                        easing = LinearEasing
                    )
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f)
    ) {

        // OPENING BURST
        burstButterflies.forEachIndexed { index, butterfly ->
            Text(
                text = "🦋",
                fontSize = (18 + (index % 4) * 3).sp,
                modifier = Modifier
                    .offset(
                        x = butterfly.first.value.dp,
                        y = butterfly.second.value.dp
                    )
                    .alpha(butterfly.third.value)
            )
        }

        // CONTINUOUS BUTTERFLY #1
        SwirlingButterfly(
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )

        // CONTINUOUS BUTTERFLY #2
        SecondFlyingButterfly(
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )
    }
}
@Composable
fun SwirlingButterfly(
    screenWidth: Float,
    screenHeight: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "swirlOne")

    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 6500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "angleOne"
    )

    val centerX = screenWidth / 2f
    val centerY = screenHeight / 3f

    val x = centerX +
            cos(angle.toDouble()).toFloat() * 120f +
            cos((angle * 3).toDouble()).toFloat() * 28f

    val y = centerY +
            sin(angle.toDouble()).toFloat() * 80f +
            sin((angle * 2).toDouble()).toFloat() * 24f

    Text(
        text = "🦋",
        fontSize = 28.sp,
        modifier = Modifier.offset(
            x = x.dp,
            y = y.dp
        )
    )
}
@Composable
fun SecondFlyingButterfly(
    screenWidth: Float,
    screenHeight: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "swirlTwo")

    val angle by infiniteTransition.animateFloat(
        initialValue = (Math.PI).toFloat(),
        targetValue = (Math.PI * 3).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 8000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "angleTwo"
    )

    val centerX = screenWidth / 2f
    val centerY = screenHeight / 2f

    val x = centerX +
            cos(angle.toDouble()).toFloat() * 150f +
            sin((angle * 2).toDouble()).toFloat() * 35f

    val y = centerY +
            sin(angle.toDouble()).toFloat() * 110f +
            cos((angle * 3).toDouble()).toFloat() * 30f

    Text(
        text = "🦋",
        fontSize = 24.sp,
        modifier = Modifier.offset(
            x = x.dp,
            y = y.dp
        )
    )
}