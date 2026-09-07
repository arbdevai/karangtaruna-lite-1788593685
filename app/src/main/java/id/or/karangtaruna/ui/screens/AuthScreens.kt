package id.or.karangtaruna.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import id.or.karangtaruna.ui.AuthViewModel

@Composable fun AuthScreen(vm: AuthViewModel) {
    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val submitState by vm.submit.collectAsState()
    val activity = LocalContext.current as? Activity
    val isRegister = mode == AuthMode.REGISTER
    val isReset = mode == AuthMode.RESET
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f),
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .7f),
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor = MaterialTheme.colorScheme.primary,
        errorTextColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error,
        errorBorderColor = MaterialTheme.colorScheme.error,
    )

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Karang Taruna", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Text(
            when (mode) { AuthMode.LOGIN -> "Masuk Aplikasi"; AuthMode.REGISTER -> "Daftar Akun Baru"; AuthMode.RESET -> "Atur Ulang Kata Sandi" },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))

        if (isRegister) {
            AuthField("Nama", name, { name = it }, fieldColors, submitState.fieldErrors["name"])
            Spacer(Modifier.height(12.dp))
        }
        AuthField("Email", email, { email = it }, fieldColors, submitState.fieldErrors["email"], KeyboardType.Email)

        if (!isReset) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Kata Sandi") },
                colors = fieldColors,
                isError = submitState.fieldErrors.containsKey("password"),
                supportingText = submitState.fieldErrors["password"]?.let { message -> { Text(message) } },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, if (showPassword) "Sembunyikan kata sandi" else "Lihat kata sandi")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { when (mode) { AuthMode.LOGIN -> vm.login(email, password); AuthMode.REGISTER -> vm.register(name, email, password); AuthMode.RESET -> vm.reset(email) } },
            enabled = !submitState.loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (submitState.loading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            else Text(if (isRegister) "Daftar" else if (isReset) "Kirim tautan" else "Masuk")
        }

        if (!isReset) {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { if (activity != null) vm.loginWithGoogle(activity) },
                enabled = !submitState.loading && activity != null,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Lanjutkan dengan Google", color = MaterialTheme.colorScheme.onSurface) }
        }

        submitState.error?.let { errorText -> Spacer(Modifier.height(12.dp)); Text(errorText, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth()) }
        submitState.success?.let { successText -> Spacer(Modifier.height(12.dp)); Text(successText, color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) }
        Spacer(Modifier.height(10.dp))
        if (mode == AuthMode.LOGIN) {
            TextButton(onClick = { mode = AuthMode.RESET; vm.clear() }) { Text("Lupa kata sandi?") }
            TextButton(onClick = { mode = AuthMode.REGISTER; vm.clear() }) { Text("Belum punya akun? Daftar") }
        } else {
            TextButton(onClick = { mode = AuthMode.LOGIN; vm.clear() }) { Text("Kembali ke masuk") }
        }
    }
}

private enum class AuthMode { LOGIN, REGISTER, RESET }

@Composable private fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    colors: TextFieldColors,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        colors = colors,
        isError = error != null,
        supportingText = error?.let { message -> { Text(message) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
    )
}
