package id.or.karangtaruna.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.or.karangtaruna.core.model.UserProfile
import id.or.karangtaruna.ui.AuthViewModel

@Composable fun ProfileScreen(profile: UserProfile, authVm: AuthViewModel, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Profil", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF151515))
        Spacer(Modifier.height(24.dp))
        Text(profile.displayName.ifBlank { "Warga" }, style = MaterialTheme.typography.titleLarge, color = Color(0xFF151515))
        Spacer(Modifier.height(6.dp))
        Text(profile.email, color = Color(0xFF505A55))
        Spacer(Modifier.height(18.dp))
        HorizontalDivider(color = Color(0xFFE5E5E5))
        Spacer(Modifier.height(18.dp))
        ProfileRow("Peran", roleLabel(profile.role))
        ProfileRow("Status", if (profile.active) "Aktif" else "Tidak aktif")
        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = { authVm.logout() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9F2F2A))) { Text("Keluar") }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Kembali", color = Color(0xFF176B53)) }
    }
}

@Composable private fun ProfileRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), color = Color(0xFF505A55))
        Text(value, color = Color(0xFF151515))
    }
}

private fun roleLabel(role: id.or.karangtaruna.core.model.Role): String = when (role) {
    id.or.karangtaruna.core.model.Role.ADMIN -> "Admin"
    id.or.karangtaruna.core.model.Role.TREASURER -> "Bendahara"
    id.or.karangtaruna.core.model.Role.VIEWER -> "Viewer"
}

@Composable fun ProfileErrorScreen(message: String, authVm: AuthViewModel) {
    val submit = authVm.submit.collectAsState().value
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Profil belum tersimpan", style = MaterialTheme.typography.titleLarge, color = Color(0xFF151515))
        Spacer(Modifier.height(10.dp))
        Text(message, color = Color(0xFF9F2F2A))
        Spacer(Modifier.height(20.dp))
        Button(onClick = { authVm.retryProfile() }, enabled = !submit.loading, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF176B53), contentColor = Color.White)) {
            if (submit.loading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp) else Text("Coba lagi")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = { authVm.logout() }, modifier = Modifier.fillMaxWidth()) { Text("Keluar", color = Color(0xFF9F2F2A)) }
    }
}
