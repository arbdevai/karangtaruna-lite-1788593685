package id.or.karangtaruna.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import id.or.karangtaruna.ui.AuthViewModel

@Composable fun AuthScreen(vm: AuthViewModel) {
    var isRegister by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val submitState by vm.submit.collectAsState()

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Karang Taruna", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(if (isRegister) "Daftar Akun Baru" else "Masuk Aplikasi", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        if (isRegister) {
            OutlinedTextField(name, { name = it }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
        }
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Kata Sandi") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { if (isRegister) vm.register(name, email, password) else vm.login(email, password) },
            enabled = !submitState.loading && email.isNotBlank() && password.isNotBlank() && (!isRegister || name.isNotBlank()),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (submitState.loading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text(if (isRegister) "Daftar" else "Masuk")
        }
        submitState.error?.let { Spacer(Modifier.height(12.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
        submitState.success?.let { Spacer(Modifier.height(12.dp)); Text(it, color = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(14.dp))
        TextButton(onClick = { isRegister = !isRegister; vm.clear() }) { Text(if (isRegister) "Sudah punya akun? Masuk" else "Belum punya akun? Daftar") }
    }
}
