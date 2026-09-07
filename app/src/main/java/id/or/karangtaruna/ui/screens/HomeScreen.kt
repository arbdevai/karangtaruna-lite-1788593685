package id.or.karangtaruna.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.or.karangtaruna.core.model.*
import id.or.karangtaruna.core.util.Formatters
import id.or.karangtaruna.ui.components.*
import id.or.karangtaruna.ui.theme.AppColors

@Composable fun HomeScreen(summary: FinanceSummary, rt: String, recentTx: List<Transaction>, onNavigate: (String) -> Unit, onRefresh: () -> Unit = {}) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Karang Taruna", color = AppColors.textPrimary, style = MaterialTheme.typography.titleLarge)
                    Text("${rt.ifBlank { "RT" }} · Bulan ini", color = AppColors.textSecondary, style = MaterialTheme.typography.bodyMedium)
                }
                TextButton(onClick = onRefresh) { Text("Segarkan", color = AppColors.accent) }
            }
        }
        item { Spacer(Modifier.height(12.dp)); CashCard(summary.balance, rt) }
        item {
            Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppOutlinedButton("Pemasukan") { onNavigate("tx/in") }
                AppOutlinedButton("Pengeluaran") { onNavigate("tx/out") }
                AppOutlinedButton("Iuran") { onNavigate("dues") }
            }
        }
        item {
            AppSection("Ringkasan bulan ini")
            StatLine("Pemasukan", "+${Formatters.rupiah(summary.income)}", AppColors.positive)
            StatLine("Pengeluaran", "-${Formatters.rupiah(summary.expense)}", AppColors.negative)
        }
        item { AppSection("Transaksi terbaru", "Lihat semua") { onNavigate("tx_list") } }
        if (recentTx.isEmpty()) item { EmptyState("Belum ada transaksi.") }
        items(recentTx, key = { it.id }) { tx ->
            Row(Modifier.fillMaxWidth().clickable { onNavigate("tx_list") }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tx.description.ifBlank { tx.category }, color = AppColors.textPrimary, style = MaterialTheme.typography.bodyLarge)
                    Text("${tx.category} · ${Formatters.relativeTime(tx.transactionDate)}", color = AppColors.textTertiary, style = MaterialTheme.typography.labelMedium)
                }
                Text((if (tx.type == TransactionType.INCOME) "+" else "-") + Formatters.rupiah(tx.amount), color = if (tx.type == TransactionType.INCOME) AppColors.positive else AppColors.negative, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = AppColors.divider)
        }
    }
}
