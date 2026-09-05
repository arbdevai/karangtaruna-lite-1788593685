package id.or.karangtaruna.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@Composable fun HomeScreen(
    summary: FinanceSummary,
    rt: String,
    recentTx: List<Transaction>,
    onNavigate: (String) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("Karang Taruna ${rt.ifBlank { "RT" }}", style = MaterialTheme.typography.titleLarge); Text("Bulan ini", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        item { CashCard(summary.balance, rt) }
        item {
            Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { onNavigate("tx/in") }, Modifier.weight(1f)) { Text("Pemasukan") }
                OutlinedButton(onClick = { onNavigate("tx/out") }, Modifier.weight(1f)) { Text("Pengeluaran") }
                OutlinedButton(onClick = { onNavigate("dues") }, Modifier.weight(1f)) { Text("Iuran") }
            }
        }
        item {
            StatLine("Pemasukan bulan ini", "+${Formatters.rupiah(summary.income)}", Color(0xFF1B6B52))
            StatLine("Pengeluaran bulan ini", "-${Formatters.rupiah(summary.expense)}", MaterialTheme.colorScheme.error)
        }
        item { SectionTitle("Transaksi terbaru", "Lihat semua") { onNavigate("tx_list") } }
        if (recentTx.isEmpty()) item { EmptyState("Belum ada transaksi.") }
        items(recentTx, key = { it.id }) { tx ->
            Row(Modifier.fillMaxWidth().clickable { onNavigate("tx_list") }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tx.description.ifBlank { tx.category }, style = MaterialTheme.typography.bodyLarge)
                    Text("${tx.category} · ${Formatters.relativeTime(tx.transactionDate)}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                }
                Text((if (tx.type == TransactionType.INCOME) "+" else "-") + Formatters.rupiah(tx.amount), color = if (tx.type == TransactionType.INCOME) Color(0xFF1B6B52) else MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        }
    }
}
