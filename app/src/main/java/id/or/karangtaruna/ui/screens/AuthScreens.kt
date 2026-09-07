package id.or.karangtaruna.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

    // CRITICAL: use explicit, high-contrast colors. Never rely on onSurfaceVariant/alpha.
    // In light theme, #F4F5F6 and #FFFFFF are almost identical — subtle Material tokens
    // become invisible. We use solid, opaque values with strong contrast ratios.
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF151515),
        unfocusedTextColor = Color(0xFF151515),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,
        disabledTextColor = Color(0xFF8A8A8A),
        errorContainerColor = Color.White,
        focusedBorderColor = Color(0xFF176B53),
        unfocusedBorderColor = Color(0xFF505A55),
        errorBorderColor = Color(0xFF9F2F2A),
        focusedLabelColor = Color(0xFF176B53),
        unfocusedLabelColor = Color(0xFF505A55),
        errorLabelColor = Color(0xFF9F2F2A),
        cursorColor = Color(0xFF176B53),
        errorCursorColor = Color(0xFF9F2F2A),
        focusedTrailingIconColor = Color(0xFF505A55),
        unfocusedTrailingIconColor = Color(0xFF505A55),
        errorTrailingIconColor = Color(0xFF9F2F2A),
    )

    // Root must be opaque with explicit colors. Transparent Column over Window background (#F4F5F6)
    // plus theme alpha inheritance causes the “white-on-white” washed-out bug.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Karang Taruna", style = MaterialTheme.typography.titleLarge, color = Color(0xFF151515), fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            when (mode) {
                AuthMode.LOGIN -> "Masuk Aplikasi"
                AuthMode.REGISTER -> "Daftar Akun Baru"
                AuthMode.RESET -> "Atur Ulang Kata Sandi"
            },
            color = Color(0xFF505A55),
            style = MaterialTheme.typography.bodyLarge,
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
                label = { Text("Kata Sandi", color = Color(0xFF505A55)) },
                colors = fieldColors,
                isError = submitState.fieldErrors.containsKey("password"),
                supportingText = submitState.fieldErrors["password"]?.let { message ->
                    { Text(message, color = Color(0xFF9F2F2A)) }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showPassword) "Sembunyikan kata sandi" else "Lihat kata sandi",
                            tint = Color(0xFF505A55),
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(22.dp))

        // Primary action fills with readable text. Never place a disabled state alpha over the whole form.
        Button(
            onClick = { when (mode) { AuthMode.LOGIN -> vm.login(email, password); AuthMode.REGISTER -> vm.register(name, email, password); AuthMode.RESET -> vm.reset(email) } },
            enabled = !submitState.loading,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF176B53), contentColor = Color.White, disabledContainerColor = Color(0xFFB9C0BB), disabledContentColor = Color.White),
        ) {
            if (submitState.loading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            else Text(if (isRegister) "Daftar" else if (isReset) "Kirim tautan" else "Masuk", fontWeight = FontWeight.SemiBold)
        }

        // Google sign-in — OutlinedButton with explicitly colored label
        if (!isReset) {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFE5E5E5))
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { if (activity != null) vm.loginWithGoogle(activity) },
                enabled = !submitState.loading && activity != null,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color(0xFF151515), disabledContainerColor = Color.White, disabledContentColor = Color(0xFF8A8A8A)),
                border = BorderStroke(1.dp, if (!submitState.loading && activity != null) Color(0xFF505A55) else Color(0xFFB7C0BB)),
            ) { Text("Lanjutkan dengan Google", color = if (submitState.loading || activity == null) Color(0xFF8A8A8A) else Color(0xFF151515), fontWeight = FontWeight.SemiBold) }
            if (activity == null) {
                Spacer(Modifier.height(6.dp))
                Text("Login Google membutuhkan Activity.", color = Color(0xFF9F2F2A), style = MaterialTheme.typography.labelLarge)
            }
        }

        submitState.error?.let { errorText -> Spacer(Modifier.height(14.dp)); Surface(color = Color(0xFFFFDAD6), shape = MaterialTheme.shapes.extraSmall, modifier = Modifier.fillMaxWidth()) { Text(errorText, Modifier.padding(12.dp), color = Color(0xFF9F2F2A), style = MaterialTheme.typography.bodyMedium) } }
        submitState.success?.let { successText -> Spacer(Modifier.height(14.dp)); Surface(color = Color(0xFFD3F0E4), shape = MaterialTheme.shapes.extraSmall, modifier = Modifier.fillMaxWidth()) { Text(successText, Modifier.padding(12.dp), color = Color(0xFF002117), style = MaterialTheme.typography.bodyMedium) } }

        Spacer(Modifier.height(12.dp))
        if (mode == AuthMode.LOGIN) {
            TextButton(onClick = { mode = AuthMode.RESET; vm.clear() }) { Text("Lupa kata sandi?", color = Color(0xFF176B53)) }
            TextButton(onClick = { mode = AuthMode.REGISTER; vm.clear() }) { Text("Belum punya akun? Daftar", color = Color(0xFF176B53)) }
        } else {
            TextButton(onClick = { mode = AuthMode.LOGIN; vm.clear() }) { Text("Kembali ke masuk", color = Color(0xFF176B53)) }
        }
    }
}

private enum class AuthMode { LOGIN, REGISTER, RESET }

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

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
        label = { Text(label, color = Color(0xFF505A55)) },
        colors = colors,
        isError = error != null,
        supportingText = error?.let { message -> { Text(message, color = Color(0xFF9F2F2A)) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
    )
}
