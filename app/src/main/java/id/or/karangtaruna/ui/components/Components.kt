package id.or.karangtaruna.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import id.or.karangtaruna.ui.theme.AppColors

@Composable fun AppSection(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = AppColors.textPrimary, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 4.dp)) { Text(action, color = AppColors.accent) }
    }
}

@Composable fun AppButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent, contentColor = Color(0xFF06251A), disabledContainerColor = AppColors.subtle, disabledContentColor = AppColors.textTertiary)) { Text(text, fontWeight = FontWeight.SemiBold) }
}

@Composable fun AppOutlinedButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = Modifier.height(44.dp), shape = RoundedCornerShape(12.dp), border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.textPrimary, disabledContentColor = AppColors.textTertiary)) { Text(text) }
}

@Composable fun AppTextField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, supporting: String? = null, isError: Boolean = false) {
    OutlinedTextField(value, onValueChange, label = { Text(label) }, modifier = modifier.fillMaxWidth(), singleLine = true, isError = isError, supportingText = supporting?.let { { Text(it) } }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = AppColors.textPrimary, unfocusedTextColor = AppColors.textPrimary, focusedContainerColor = AppColors.surface, unfocusedContainerColor = AppColors.surface, focusedBorderColor = AppColors.accent, unfocusedBorderColor = AppColors.outline, focusedLabelColor = AppColors.accent, unfocusedLabelColor = AppColors.textSecondary, cursorColor = AppColors.accent))
}

@Composable fun CashCard(balance: Long, rt: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().aspectRatio(1.58f).background(Brush.linearGradient(listOf(Color(0xFF252C28), Color(0xFF111412))), RoundedCornerShape(24.dp)).padding(22.dp)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("KARTU KAS", color = AppColors.textSecondary, style = MaterialTheme.typography.labelLarge); Text(rt.ifBlank { "RT" }, color = AppColors.textSecondary, style = MaterialTheme.typography.labelLarge) }
            Spacer(Modifier.weight(1f)); Text("••••  ••••  ••••  004", color = AppColors.textSecondary, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp)); Text("SALDO KAS", color = AppColors.textTertiary, style = MaterialTheme.typography.labelMedium)
            Text(Formatters.rupiah(balance), color = AppColors.textPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable fun EmptyState(text: String) { Text(text, Modifier.fillMaxWidth().padding(vertical = 24.dp), color = AppColors.textSecondary) }
@Composable fun ErrorState(text: String, retry: () -> Unit) { Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Text(text, Modifier.weight(1f), color = AppColors.negative); TextButton(onClick = retry) { Text("Coba lagi", color = AppColors.accent) } } }
@Composable fun StatLine(label: String, value: String, valueColor: Color = AppColors.textPrimary, onClick: (() -> Unit)? = null) { Column(Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(vertical = 11.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Text(label, Modifier.weight(1f), color = AppColors.textSecondary, style = MaterialTheme.typography.bodyMedium); Text(value, color = valueColor, fontWeight = FontWeight.SemiBold) }; HorizontalDivider(Modifier.padding(top = 11.dp), color = AppColors.divider) } }
