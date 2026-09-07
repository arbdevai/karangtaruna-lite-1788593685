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
import id.or.karangtaruna.ui.theme.AppColors

@Composable fun TransactionsScreen(vm: ModuleViewModel, onAdd: (TransactionType) -> Unit) {
    val state by vm.transactions.collectAsState()
    LaunchedEffect(Unit) { vm.loadTransactions(refresh = true) }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Transaksi", style = MaterialTheme.typography.headlineSmall, color = AppColors.textPrimary); AppOutlinedButton("Tambah") { onAdd(TransactionType.INCOME) } }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { AppOutlinedButton("Semua") { vm.loadTransactions(refresh = true) }; AppOutlinedButton("Pemasukan") { vm.loadTransactions(refresh = true, type = TransactionType.INCOME) }; AppOutlinedButton("Pengeluaran") { vm.loadTransactions(refresh = true, type = TransactionType.EXPENSE) } }
        state.error?.let { ErrorState(it) { vm.loadTransactions(refresh = true) } }
        if (state.loading && state.items.isEmpty()) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 12.dp), color = AppColors.accent)
        if (!state.loading && state.items.isEmpty()) EmptyState("Belum ada transaksi.")
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) { items(state.items, key = { it.id }) { tx -> TransactionListRow(tx) }; if (state.hasMore && state.items.isNotEmpty()) item { TextButton({ vm.loadTransactions() }, Modifier.fillMaxWidth()) { Text("Muat lebih banyak", color = AppColors.accent) } } }
    }
}

@Composable private fun TransactionListRow(tx: Transaction) { StatLine("${tx.description.ifBlank { tx.category }}\n${Formatters.date(tx.transactionDate)}", "${if (tx.type == TransactionType.INCOME) "+" else "-"}${Formatters.rupiah(tx.amount)}", if (tx.type == TransactionType.INCOME) AppColors.positive else AppColors.negative) }

@Composable fun TransactionFormScreen(type: TransactionType, vm: ModuleViewModel, onSaved: () -> Unit) {
    var amountText by remember { mutableStateOf("") }; var category by remember { mutableStateOf(if (type == TransactionType.INCOME) "Iuran" else "Kegiatan") }; var description by remember { mutableStateOf("") }; var note by remember { mutableStateOf("") }
    val submit by vm.submit.collectAsState(); val amount = Formatters.digitsOnly(amountText); val error = Validation.amount(amount) ?: Validation.required(description, "Keterangan")
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(if (type == TransactionType.INCOME) "Pemasukan" else "Pengeluaran", style = MaterialTheme.typography.headlineSmall, color = AppColors.textPrimary); Spacer(Modifier.height(18.dp))
        AppTextField("Nominal", amountText, { amountText = Formatters.rupiahInput(it) }, supporting = if (amountText.isNotBlank()) "Nominal: ${Formatters.rupiah(amount ?: 0)}" else null)
        Spacer(Modifier.height(12.dp)); AppTextField("Kategori", category, { category = it }); Spacer(Modifier.height(12.dp)); AppTextField("Keterangan", description, { description = it }); Spacer(Modifier.height(12.dp)); AppTextField("Catatan (opsional)", note, { note = it })
        error?.let { Text(it, color = AppColors.negative, modifier = Modifier.padding(top = 8.dp)) }; submit.error?.let { Text(it, color = AppColors.negative, modifier = Modifier.padding(top = 8.dp)) }
        Spacer(Modifier.height(22.dp)); AppButton("Simpan transaksi", enabled = error == null && !submit.loading) { vm.saveTransaction(Transaction(type = type, amount = amount ?: 0, category = category, description = description, transactionDate = System.currentTimeMillis(), note = note.ifBlank { null })) { onSaved() } }
        submit.success?.let { Text(it, color = AppColors.positive, modifier = Modifier.padding(top = 8.dp)) }
    }
}

@Composable fun MembersScreen(vm: ModuleViewModel, onAdd: () -> Unit) {
    var search by remember { mutableStateOf("") }; val state by vm.members.collectAsState(); LaunchedEffect(Unit) { vm.loadMembers(refresh = true) }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) { Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Warga", style = MaterialTheme.typography.headlineSmall, color = AppColors.textPrimary); AppOutlinedButton("Tambah") { onAdd() } }; Spacer(Modifier.height(12.dp)); AppTextField("Cari warga", search, { search = it; vm.loadMembers(refresh = true, query = it) }); Text("${state.items.size} warga", color = AppColors.textSecondary, modifier = Modifier.padding(vertical = 12.dp)); if (!state.loading && state.items.isEmpty()) EmptyState("Belum ada data warga."); LazyColumn { items(state.items, key = { it.id }) { member -> StatLine(member.fullName, if (member.status == MemberStatus.ACTIVE) "Aktif" else "Tidak aktif", if (member.status == MemberStatus.ACTIVE) AppColors.positive else AppColors.textTertiary) }; if (state.hasMore && state.items.isNotEmpty()) item { TextButton({ vm.loadMembers() }, Modifier.fillMaxWidth()) { Text("Muat lebih banyak", color = AppColors.accent) } } } }
}

@Composable fun MemberFormScreen(vm: ModuleViewModel, onSaved: () -> Unit) { var name by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("") }; val error = Validation.memberName(name); val submit by vm.submit.collectAsState(); Column(Modifier.fillMaxSize().padding(20.dp)) { Text("Tambah warga", style = MaterialTheme.typography.headlineSmall, color = AppColors.textPrimary); Spacer(Modifier.height(18.dp)); AppTextField("Nama lengkap", name, { name = it }, isError = error != null); Spacer(Modifier.height(12.dp)); AppTextField("Nomor telepon (opsional)", phone, { phone = it }); error?.let { Text(it, color = AppColors.negative, modifier = Modifier.padding(top = 8.dp)) }; Spacer(Modifier.height(22.dp)); AppButton("Simpan warga", enabled = error == null && !submit.loading) { vm.saveMember(Member(fullName = name, phoneNumber = phone.ifBlank { null })); onSaved() } } }

@Composable fun DuesScreen(vm: ModuleViewModel, onAdd: () -> Unit) { val periodId = java.time.LocalDate.now().let { "%04d-%02d".format(it.year, it.monthValue) }; val state by vm.dues.collectAsState(); LaunchedEffect(periodId) { vm.loadDues(periodId, refresh = true) }; Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) { Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Iuran", style = MaterialTheme.typography.headlineSmall, color = AppColors.textPrimary); AppOutlinedButton("Catat") { onAdd() } }; Text(Formatters.monthYear(periodId.substringBefore('-').toInt(), periodId.substringAfter('-').toInt()), color = AppColors.textSecondary, modifier = Modifier.padding(top = 4.dp)); Spacer(Modifier.height(14.dp)); val paid = state.items.count { it.status == DuesStatus.PAID }; Text("$paid dari ${state.items.size} sudah bayar", color = AppColors.textPrimary, fontWeight = FontWeight.SemiBold); state.error?.let { ErrorState(it) { vm.loadDues(periodId, refresh = true) } }; if (!state.loading && state.items.isEmpty()) EmptyState("Belum ada pembayaran bulan ini."); LazyColumn { items(state.items, key = { it.id }) { due -> StatLine(due.memberId, if (due.status == DuesStatus.PAID) "Sudah bayar · ${Formatters.rupiah(due.amount)}" else "Belum bayar", if (due.status == DuesStatus.PAID) AppColors.positive else AppColors.textTertiary) } } } }

@Composable fun DuesFormScreen(vm: ModuleViewModel, onSaved: () -> Unit) { val membersState by vm.members.collectAsState(); LaunchedEffect(Unit) { vm.loadMembers(refresh = true) }; var selectedMember by remember { mutableStateOf<Member?>(null) }; var amountText by remember { mutableStateOf("") }; var note by remember { mutableStateOf("") }; var expanded by remember { mutableStateOf(false) }; val period = java.time.LocalDate.now().let { "%04d-%02d".format(it.year, it.monthValue) }; val amount = Formatters.digitsOnly(amountText); val submit by vm.submit.collectAsState(); val error = if (selectedMember == null) "Pilih warga terlebih dahulu." else Validation.amount(amount)
    Column(Modifier.fillMaxSize().padding(20.dp)) { Text("Catat iuran", style = MaterialTheme.typography.headlineSmall, color = AppColors.textPrimary); Spacer(Modifier.height(18.dp)); AppOutlinedButton(selectedMember?.fullName ?: "Pilih warga") { expanded = true }; DropdownMenu(expanded, { expanded = false }) { membersState.items.forEach { member -> DropdownMenuItem(text = { Text(member.fullName) }, onClick = { selectedMember = member; expanded = false }) } }; Spacer(Modifier.height(12.dp)); AppTextField("Nominal", amountText, { amountText = Formatters.rupiahInput(it) }, supporting = if (amountText.isNotBlank()) "Nominal: ${Formatters.rupiah(amount ?: 0)}" else null); Spacer(Modifier.height(12.dp)); AppTextField("Catatan (opsional)", note, { note = it }); error?.let { Text(it, color = AppColors.negative, modifier = Modifier.padding(top = 8.dp)) }; submit.error?.let { Text(it, color = AppColors.negative, modifier = Modifier.padding(top = 8.dp)) }; Spacer(Modifier.height(22.dp)); AppButton("Simpan pembayaran", enabled = error == null && !submit.loading) { val member = selectedMember ?: return@AppButton; vm.payDues(period, member.id, amount ?: 0, System.currentTimeMillis(), note.ifBlank { null }); onSaved() } }
}
