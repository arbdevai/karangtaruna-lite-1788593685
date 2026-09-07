package id.or.karangtaruna.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.or.karangtaruna.core.auth.Validation
import id.or.karangtaruna.core.model.*
import id.or.karangtaruna.core.util.Formatters
import id.or.karangtaruna.ui.ModuleViewModel
import id.or.karangtaruna.ui.components.*

@Composable fun TransactionsScreen(vm: ModuleViewModel, onAdd: (TransactionType) -> Unit) {
    val state by vm.transactions.collectAsState()
    LaunchedEffect(Unit) { vm.loadTransactions(refresh = true) }
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 17.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Transaksi", style = MaterialTheme.typography.headlineSmall); TextButton({ onAdd(TransactionType.INCOME) }) { Text("Tambah") } }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton({ vm.loadTransactions(refresh = true) }) { Text("Semua") }; OutlinedButton({ vm.loadTransactions(refresh = true, type = TransactionType.INCOME) }) { Text("Pemasukan") }; OutlinedButton({ vm.loadTransactions(refresh = true, type = TransactionType.EXPENSE) }) { Text("Pengeluaran") } }
        state.error?.let { ErrorState(it) { vm.loadTransactions(refresh = true) } }
        if (state.loading && state.items.isEmpty()) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 12.dp))
        if (!state.loading && state.items.isEmpty()) EmptyState("Belum ada transaksi.")
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            items(state.items, key = { it.id }) { tx -> TransactionListRow(tx) }
            if (state.hasMore && state.items.isNotEmpty()) item { TextButton({ vm.loadTransactions() }, Modifier.fillMaxWidth()) { Text("Muat lebih banyak") } }
        }
    }
}

@Composable private fun TransactionListRow(tx: Transaction) { StatLine("${tx.description.ifBlank { tx.category }}\n${Formatters.date(tx.transactionDate)}", "${if (tx.type == TransactionType.INCOME) "+" else "-"}${Formatters.rupiah(tx.amount)}", if (tx.type == TransactionType.INCOME) Color(0xFF1B6B52) else MaterialTheme.colorScheme.error) }

@Composable fun TransactionFormScreen(type: TransactionType, vm: ModuleViewModel, onSaved: () -> Unit) {
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(if (type == TransactionType.INCOME) "Iuran" else "Kegiatan") }
    var description by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val submit by vm.submit.collectAsState()
    val amount = Formatters.digitsOnly(amountText)
    val validationError = Validation.amount(amount) ?: Validation.required(description, "Keterangan")
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(if (type == TransactionType.INCOME) "Pemasukan" else "Pengeluaran", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(amountText, { amountText = Formatters.rupiahInput(it) }, label = { Text("Nominal") }, prefix = { Text("Rp ") }, supportingText = { if (amountText.isNotBlank()) Text("Nominal: ${Formatters.rupiah(amount ?: 0)}") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp)); OutlinedTextField(category, { category = it }, label = { Text("Kategori") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp)); OutlinedTextField(description, { description = it }, label = { Text("Keterangan") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp)); OutlinedTextField(note, { note = it }, label = { Text("Catatan (opsional)") }, modifier = Modifier.fillMaxWidth())
        validationError?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
        submit.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
        Spacer(Modifier.height(20.dp))
        Button(onClick = { vm.saveTransaction(Transaction(type = type, amount = amount ?: 0, category = category, description = description, transactionDate = System.currentTimeMillis(), note = note.ifBlank { null })) { onSaved() } }, enabled = validationError == null && !submit.loading, modifier = Modifier.fillMaxWidth()) {
            if (submit.loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Simpan transaksi")
        }
        submit.success?.let { Text(it, color = Color(0xFF1B6B52), modifier = Modifier.padding(top = 8.dp)) }
    }
}

@Composable fun MembersScreen(vm: ModuleViewModel, onAdd: () -> Unit) {
    var search by remember { mutableStateOf("") }; val state by vm.members.collectAsState(); LaunchedEffect(Unit) { vm.loadMembers(refresh = true) }
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) { Row(Modifier.fillMaxWidth().padding(top = 17.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Warga", style = MaterialTheme.typography.headlineSmall); TextButton(onAdd) { Text("Tambah warga") } }; OutlinedTextField(search, { search = it; vm.loadMembers(refresh = true, query = it) }, label = { Text("Cari warga") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)); Text("${state.items.size} warga", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 10.dp)); if (!state.loading && state.items.isEmpty()) EmptyState("Belum ada data warga."); LazyColumn { items(state.items, key = { it.id }) { member -> StatLine(member.fullName, if (member.status == MemberStatus.ACTIVE) "Aktif" else "Tidak aktif") }; if (state.hasMore && state.items.isNotEmpty()) item { TextButton({ vm.loadMembers() }, Modifier.fillMaxWidth()) { Text("Muat lebih banyak") } } } }
}

@Composable fun MemberFormScreen(vm: ModuleViewModel, onSaved: () -> Unit) { var name by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("") }; val error = Validation.memberName(name); val submit by vm.submit.collectAsState(); Column(Modifier.fillMaxSize().padding(20.dp)) { Text("Tambah warga", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(18.dp)); OutlinedTextField(name, { name = it }, label = { Text("Nama lengkap") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(10.dp)); OutlinedTextField(phone, { phone = it }, label = { Text("Nomor telepon (opsional)") }, modifier = Modifier.fillMaxWidth()); if (error != null) Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)); Spacer(Modifier.height(20.dp)); Button(onClick = { vm.saveMember(Member(fullName = name, phoneNumber = phone.ifBlank { null })); onSaved() }, enabled = error == null && !submit.loading, modifier = Modifier.fillMaxWidth()) { Text("Simpan") } } }

@Composable fun DuesScreen(vm: ModuleViewModel, onAdd: () -> Unit) { val periodId = java.time.LocalDate.now().let { "%04d-%02d".format(it.year, it.monthValue) }; val state by vm.dues.collectAsState(); LaunchedEffect(periodId) { vm.loadDues(periodId, refresh = true) }; Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) { Row(Modifier.fillMaxWidth().padding(top = 17.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Iuran", style = MaterialTheme.typography.headlineSmall); TextButton(onAdd) { Text("Catat iuran") } }; Text(Formatters.monthYear(periodId.substringBefore('-').toInt(), periodId.substringAfter('-').toInt()), color = MaterialTheme.colorScheme.onSurfaceVariant); val paid = state.items.count { it.status == DuesStatus.PAID }; Text("$paid dari ${state.items.size} sudah bayar", modifier = Modifier.padding(top = 16.dp), fontWeight = FontWeight.SemiBold); if (!state.loading && state.items.isEmpty()) EmptyState("Belum ada pembayaran bulan ini."); LazyColumn { items(state.items, key = { it.id }) { due -> StatLine(due.memberId, if (due.status == DuesStatus.PAID) "Sudah bayar · ${Formatters.rupiah(due.amount)}" else "Belum bayar", if (due.status == DuesStatus.PAID) Color(0xFF1B6B52) else MaterialTheme.colorScheme.onSurfaceVariant) } } } }

@Composable fun DuesFormScreen(vm: ModuleViewModel, onSaved: () -> Unit) {
    val membersState by vm.members.collectAsState()
    LaunchedEffect(Unit) { vm.loadMembers(refresh = true) }
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var memberMenuExpanded by remember { mutableStateOf(false) }
    val period = java.time.LocalDate.now().let { "%04d-%02d".format(it.year, it.monthValue) }
    val amount = Formatters.digitsOnly(amountText)
    val submit by vm.submit.collectAsState()
    val validationError = when {
        selectedMember == null -> "Pilih warga terlebih dahulu."
        amount == null || amount <= 0 -> "Nominal iuran harus lebih dari Rp0."
        else -> null
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Catat iuran", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(18.dp))
        OutlinedButton(onClick = { memberMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selectedMember?.fullName ?: "Pilih Warga")
        }
        DropdownMenu(expanded = memberMenuExpanded, onDismissRequest = { memberMenuExpanded = false }) {
            membersState.items.forEach { member ->
                DropdownMenuItem(text = { Text(member.fullName) }, onClick = {
                    selectedMember = member
                    memberMenuExpanded = false
                })
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(amountText, { amountText = Formatters.rupiahInput(it) }, label = { Text("Nominal") }, prefix = { Text("Rp ") }, supportingText = { if (amountText.isNotBlank()) Text("Nominal: ${Formatters.rupiah(amount ?: 0)}") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(note, { note = it }, label = { Text("Catatan (opsional)") }, modifier = Modifier.fillMaxWidth())
        validationError?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
        submit.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
        Spacer(Modifier.height(20.dp))
        Button(onClick = {
            val member = selectedMember ?: return@Button
            vm.payDues(period, member.id, amount ?: 0, System.currentTimeMillis(), note.ifBlank { null })
            onSaved()
        }, enabled = validationError == null && !submit.loading, modifier = Modifier.fillMaxWidth()) {
            if (submit.loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Simpan pembayaran")
        }
    }
}
