package id.or.karangtaruna.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.or.karangtaruna.core.util.Formatters

@Composable fun CashCard(balance: Long, rt: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().aspectRatio(1.62f).background(Brush.linearGradient(listOf(Color(0xFF2E3332), Color(0xFF111414))), RoundedCornerShape(22.dp)).padding(22.dp)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("KARANG TARUNA", color = Color(0xFFDDE4DF), style = MaterialTheme.typography.labelLarge); Text(rt.ifBlank { "RT —" }, color = Color(0xFF9CA7A0), style = MaterialTheme.typography.labelLarge) }
            Spacer(Modifier.weight(1f))
            Text("KARTU KAS", color = Color(0xFF9CA7A0), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(7.dp)); Text("••••  ••••  ••••  004", color = Color(0xFFB9C5BE), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(18.dp)); Text("SALDO KAS", color = Color(0xFF9CA7A0), style = MaterialTheme.typography.labelLarge)
            Text(Formatters.rupiah(balance), color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable fun SectionTitle(title: String, action: String? = null, onAction: (() -> Unit)? = null) { Row(Modifier.fillMaxWidth().padding(top = 23.dp, bottom = 9.dp), verticalAlignment = Alignment.CenterVertically) { Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f)); if (action != null && onAction != null) TextButton(onClick = onAction) { Text(action) } } }
@Composable fun EmptyState(text: String) { Text(text, Modifier.fillMaxWidth().padding(vertical = 25.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
@Composable fun ErrorState(text: String, retry: () -> Unit) { Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Text(text, Modifier.weight(1f), color = MaterialTheme.colorScheme.error); TextButton(retry) { Text("Coba lagi") } } }
@Composable fun StatLine(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, color = valueColor, fontWeight = FontWeight.SemiBold)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
}
