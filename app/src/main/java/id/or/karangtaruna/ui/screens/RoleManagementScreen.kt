package id.or.karangtaruna.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.or.karangtaruna.core.model.Role
import id.or.karangtaruna.core.model.UserProfile
import id.or.karangtaruna.ui.ModuleViewModel

@Composable fun RoleManagementScreen(vm: ModuleViewModel, onBack: () -> Unit) {
    val state by vm.users.collectAsState()
    LaunchedEffect(Unit) { vm.loadUsers(refresh = true) }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Kelola peran", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF151515))
            TextButton(onClick = onBack) { Text("Kembali", color = Color(0xFF176B53)) }
        }
        Spacer(Modifier.height(10.dp))
        state.error?.let { Text(it, color = Color(0xFF9F2F2A)) }
        if (state.loading && state.items.isEmpty()) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (!state.loading && state.items.isEmpty()) Text("Belum ada pengguna.", color = Color(0xFF505A55), modifier = Modifier.padding(vertical = 24.dp))
        LazyColumn { items(state.items, key = { it.uid }) { user -> RoleRow(user, vm) } }
    }
}

@Composable private fun RoleRow(user: UserProfile, vm: ModuleViewModel) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(user.displayName.ifBlank { "Warga" }, color = Color(0xFF151515))
                Text(user.email, color = Color(0xFF505A55))
            }
            TextButton(onClick = { expanded = !expanded }) { Text(roleLabel(user.role), color = Color(0xFF176B53)) }
        }
        if (expanded) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Role.values().forEach { role ->
                    OutlinedButton(onClick = { vm.updateUserRole(user.uid, role); expanded = false }) { Text(roleLabel(role)) }
                }
            }
        }
        HorizontalDivider(color = Color(0xFFE5E5E5))
    }
}

private fun roleLabel(role: Role) = when (role) { Role.ADMIN -> "Admin"; Role.TREASURER -> "Bendahara"; Role.VIEWER -> "Viewer" }
