package id.or.karangtaruna.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.or.karangtaruna.core.model.*
import id.or.karangtaruna.data.AuthRepository
import id.or.karangtaruna.data.OrganizationRepository
import id.or.karangtaruna.data.SessionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SubmitState(val loading: Boolean = false, val error: String? = null, val success: String? = null)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    val session: StateFlow<SessionState> = repository.session
    private val _submit = MutableStateFlow(SubmitState())
    val submit = _submit.asStateFlow()
    fun login(email: String, password: String) = execute { repository.login(email, password) }
    fun register(name: String, email: String, password: String) = execute("Pendaftaran berhasil.") { repository.register(name, email, password) }
    fun reset(email: String) = execute("Tautan pengaturan ulang telah dikirim.") { repository.resetPassword(email) }
    fun logout() = repository.logout()
    fun clear() { _submit.value = SubmitState() }
    private fun execute(success: String? = null, block: suspend () -> AppResult<Unit>) {
        if (_submit.value.loading) return
        viewModelScope.launch {
            _submit.value = SubmitState(loading = true)
            _submit.value = when (val r = block()) {
                is AppResult.Success -> SubmitState(success = success)
                is AppResult.Failure -> SubmitState(error = r.message)
            }
        }
    }
}

data class HomeState(
    val loading: Boolean = true,
    val balance: Long = 0,
    val incomeMonth: Long = 0,
    val expenseMonth: Long = 0,
    val duesPaid: Int = 0,
    val duesTotal: Int = 0,
    val duesAmount: Long = 0,
    val transactions: List<Transaction> = emptyList(),
    val error: String? = null,
)

class HomeViewModel(private val repository: OrganizationRepository) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()
    init { refresh() }
    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val summary = repository.observeSummary()
            val txResult = repository.transactions(limit = 5)
            val periodId = currentPeriodId()
            val duesResult = repository.dues(periodId, limit = 100)
            if (summary is AppResult.Failure) _state.value = _state.value.copy(loading = false, error = summary.message)
            else {
                val finance = (summary as? AppResult.Success)?.data ?: FinanceSummary()
                val txs = (txResult as? AppResult.Success)?.data?.items ?: emptyList()
                val dues = (duesResult as? AppResult.Success)?.data?.items ?: emptyList()
                val paid = dues.count { it.status == DuesStatus.PAID }
                val totalAmount = dues.filter { it.status == DuesStatus.PAID }.sumOf { it.amount }
                _state.value = HomeState(false, finance.balance, finance.income, finance.expense, paid, dues.size, totalAmount, txs)
            }
        }
    }
    private fun currentPeriodId(): String {
        val now = java.time.LocalDate.now()
        return "%04d-%02d".format(now.year, now.monthValue)
    }
}

data class ListState<T>(val loading: Boolean = false, val items: List<T> = emptyList(), val hasMore: Boolean = true, val error: String? = null)

class ModuleViewModel(private val repository: OrganizationRepository) : ViewModel() {
    private val _transactions = MutableStateFlow(ListState<Transaction>()); val transactions = _transactions.asStateFlow()
    private val _members = MutableStateFlow(ListState<Member>()); val members = _members.asStateFlow()
    private val _dues = MutableStateFlow(ListState<DuesPayment>()); val dues = _dues.asStateFlow()
    private val _periods = MutableStateFlow(ListState<DuesPeriod>()); val periods = _periods.asStateFlow()
    private val _submit = MutableStateFlow(SubmitState()); val submit = _submit.asStateFlow()
    private val cursors = mutableMapOf<String, Any?>()
    private val jobs = mutableMapOf<String, Job>()

    fun loadTransactions(refresh: Boolean = false, type: TransactionType? = null) = load("tx:${type?.name ?: "ALL"}", _transactions, refresh) { repository.transactions(limit = 25, cursor = it, type = type) }
    fun loadMembers(refresh: Boolean = false, query: String = "") = load("members:$query", _members, refresh) { repository.members(limit = 30, cursor = it, queryText = query) }
    fun loadDues(periodId: String, refresh: Boolean = false) = load("dues:$periodId", _dues, refresh) { repository.dues(periodId, limit = 50, cursor = it) }
    fun loadPeriods(refresh: Boolean = false) = load("periods", _periods, refresh) { repository.periods(limit = 24, cursor = it) }

    fun saveTransaction(tx: Transaction) = submitOp("Transaksi tersimpan.") { repository.saveTransaction(tx) }
    fun saveMember(member: Member) = submitOp("Warga tersimpan.") { repository.saveMember(member) }
    fun payDues(periodId: String, memberId: String, amount: Long, paymentDate: Long, note: String?) = submitOp("Pembayaran tercatat.") { repository.recordDuesPayment(periodId, memberId, amount, paymentDate, note) }
    fun savePeriod(period: DuesPeriod) = submitOp("Periode tersimpan.") { repository.savePeriod(period) }
    fun saveSettings(name: String, rt: String, duesAmount: Long) = submitOp("Pengaturan tersimpan.") { repository.saveSettings(name, rt, duesAmount) }
    fun clearSubmit() { _submit.value = SubmitState() }

    private fun <T> load(key: String, target: MutableStateFlow<ListState<T>>, refresh: Boolean, request: suspend (Any?) -> AppResult<Page<T>>) {
        if (jobs[key]?.isActive == true) return
        if (!refresh && !target.value.hasMore && target.value.items.isNotEmpty()) return
        if (refresh) { cursors[key] = null; target.value = ListState(loading = true) } else target.value = target.value.copy(loading = true, error = null)
        jobs[key] = viewModelScope.launch {
            when (val r = request(cursors[key])) {
                is AppResult.Success -> {
                    val page = r.data; cursors[key] = page.cursor
                    val merged = (if (refresh) emptyList() else target.value.items) + page.items
                    target.value = ListState(items = merged.distinctBy { stableId(it as Any) }, hasMore = page.hasMore)
                }
                is AppResult.Failure -> target.value = target.value.copy(loading = false, error = r.message)
            }
        }
    }
    private fun submitOp(message: String, block: suspend () -> AppResult<Unit>) {
        if (_submit.value.loading) return
        viewModelScope.launch {
            _submit.value = SubmitState(loading = true)
            _submit.value = when (val r = block()) {
                is AppResult.Success -> SubmitState(success = message)
                is AppResult.Failure -> SubmitState(error = r.message)
            }
        }
    }
    private fun stableId(v: Any): String = when (v) {
        is Transaction -> v.id; is Member -> v.id; is DuesPayment -> v.id; is DuesPeriod -> v.id; else -> v.hashCode().toString()
    }
}
