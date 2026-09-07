package id.or.karangtaruna.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import id.or.karangtaruna.ui.AuthViewModel
import id.or.karangtaruna.ui.components.AppButton
import id.or.karangtaruna.ui.theme.AppColors

@Composable fun AuthScreen(vm: AuthViewModel) {
    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val submitState by vm.submit.collectAsState()
    val activity = LocalContext.current.findActivity()
    val isRegister = mode == AuthMode.REGISTER
    val isReset = mode == AuthMode.RESET

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = AppColors.textPrimary,
        unfocusedTextColor = AppColors.textPrimary,
        focusedContainerColor = AppColors.surface,
        unfocusedContainerColor = AppColors.surface,
        focusedBorderColor = AppColors.accent,
        unfocusedBorderColor = AppColors.outline,
        focusedLabelColor = AppColors.accent,
        unfocusedLabelColor = AppColors.textSecondary,
        cursorColor = AppColors.accent,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Karang Taruna", style = MaterialTheme.typography.displaySmall, color = AppColors.textPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            when (mode) { AuthMode.LOGIN -> "Masuk ke akun"; AuthMode.REGISTER -> "Daftar akun baru"; AuthMode.RESET -> "Atur ulang kata sandi" },
            color = AppColors.textSecondary,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(28.dp))

        if (isRegister) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama lengkap") },
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                isError = submitState.fieldErrors.containsKey("name"),
                supportingText = submitState.fieldErrors["name"]?.let { { Text(it, color = AppColors.negative) } },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            colors = fieldColors,
            shape = RoundedCornerShape(14.dp),
            isError = submitState.fieldErrors.containsKey("email"),
            supportingText = submitState.fieldErrors["email"]?.let { { Text(it, color = AppColors.negative) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )

        if (!isReset) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Kata sandi") },
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                isError = submitState.fieldErrors.containsKey("password"),
                supportingText = submitState.fieldErrors["password"]?.let { { Text(it, color = AppColors.negative) } },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null, tint = AppColors.textSecondary)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(24.dp))
        AppButton(
            text = if (isRegister) "Daftar" else if (isReset) "Kirim tautan" else "Masuk",
            enabled = !submitState.loading,
            onClick = { when (mode) { AuthMode.LOGIN -> vm.login(email, password); AuthMode.REGISTER -> vm.register(name, email, password); AuthMode.RESET -> vm.reset(email) } },
        )

        if (!isReset) {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = AppColors.divider)
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { if (activity != null) vm.loginWithGoogle(activity) },
                enabled = !submitState.loading && activity != null,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = AppColors.elevated, contentColor = AppColors.textPrimary),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
            ) { Text("Lanjutkan dengan Google", fontWeight = FontWeight.SemiBold) }
        }

        submitState.error?.let { errorText ->
            Spacer(Modifier.height(14.dp))
            Text(errorText, color = AppColors.negative, style = MaterialTheme.typography.bodyMedium)
        }
        submitState.success?.let { successText ->
            Spacer(Modifier.height(14.dp))
            Text(successText, color = AppColors.positive, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(16.dp))
        if (mode == AuthMode.LOGIN) {
            TextButton(onClick = { mode = AuthMode.RESET; vm.clear() }) { Text("Lupa kata sandi?", color = AppColors.accent) }
            TextButton(onClick = { mode = AuthMode.REGISTER; vm.clear() }) { Text("Belum punya akun? Daftar", color = AppColors.textSecondary) }
        } else {
            TextButton(onClick = { mode = AuthMode.LOGIN; vm.clear() }) { Text("Kembali ke masuk", color = AppColors.accent) }
        }
    }
}

private enum class AuthMode { LOGIN, REGISTER, RESET }

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
