package id.or.karangtaruna.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import id.or.karangtaruna.core.data.toUserMessage
import id.or.karangtaruna.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import java.util.UUID

class OrganizationRepository(private val db: FirebaseFirestore, private val uid: () -> String?) {
    fun observeSettings(): Flow<OrganizationSettings> = callbackFlow {
        val registration = db.collection("settings").document("organization").addSnapshotListener { snapshot, error ->
            if (error != null) close(error) else trySend(OrganizationSettings(snapshot?.getString("organizationName") ?: "Karang Taruna", snapshot?.getString("rtNumber").orEmpty(), snapshot?.getLong("duesDefaultAmount") ?: 0))
        }
        awaitClose { registration.remove() }
    }

    suspend fun observeSummary(): AppResult<FinanceSummary> = result {
        val summary = db.collection("settings").document("financeSummary").get().await()
        val income = summary.getLong("income") ?: 0
        val expense = summary.getLong("expense") ?: 0
        FinanceSummary(income - expense, income, expense)
    }

    suspend fun transactions(limit: Long = 30, cursor: Any? = null, type: TransactionType? = null): AppResult<Page<Transaction>> = page(db.collection("transactions").whereEqualTo("archived", false).let { if (type == null) it else it.whereEqualTo("type", type.name) }.orderBy("transactionDate", Query.Direction.DESCENDING), limit, cursor) { snap -> snap.toTransaction() }
    suspend fun members(limit: Long = 30, cursor: Any? = null, queryText: String = ""): AppResult<Page<Member>> = page(db.collection("members").whereEqualTo("status", MemberStatus.ACTIVE.name).orderBy("normalizedName"), limit, cursor) { it.toMember() }.mapItems { if (queryText.isBlank()) it else it.filter { member -> member.fullName.contains(queryText, true) } }
    suspend fun dues(periodId: String, limit: Long = 50, cursor: Any? = null): AppResult<Page<DuesPayment>> = page(db.collection("duesPayments").whereEqualTo("periodId", periodId).orderBy("memberId"), limit, cursor) { it.toDuesPayment() }
    suspend fun periods(limit: Long = 24, cursor: Any? = null): AppResult<Page<DuesPeriod>> = page(db.collection("duesPeriods").orderBy("year", Query.Direction.DESCENDING).orderBy("month", Query.Direction.DESCENDING), limit, cursor) { it.toDuesPeriod() }

    suspend fun savePeriod(period: DuesPeriod): AppResult<Unit> = result {
        val actor = uid() ?: error("Sesi berakhir")
        val id = "%04d-%02d".format(period.year, period.month)
        db.collection("duesPeriods").document(id).set(mapOf("year" to period.year, "month" to period.month, "defaultAmount" to period.defaultAmount, "active" to period.active, "createdBy" to actor, "createdAt" to FieldValue.serverTimestamp())).await()
    }

    suspend fun saveSettings(name: String, rt: String, duesAmount: Long): AppResult<Unit> = result {
        db.collection("settings").document("organization").set(mapOf("organizationName" to name.trim(), "rtNumber" to rt.trim(), "duesDefaultAmount" to duesAmount), com.google.firebase.firestore.SetOptions.merge()).await()
    }

    suspend fun saveTransaction(txn: Transaction): AppResult<Unit> = result {
        val actor = uid() ?: error("Sesi berakhir")
        val id = txn.id.ifBlank { UUID.randomUUID().toString() }
        val ref = db.collection("transactions").document(id)
        val summaryRef = db.collection("settings").document("financeSummary")
        val incomeDelta = if (txn.type == TransactionType.INCOME) txn.amount else 0L
        val expenseDelta = if (txn.type == TransactionType.EXPENSE) txn.amount else 0L
        db.runBatch { batch ->
            batch.set(ref, mapOf("type" to txn.type.name, "amount" to txn.amount, "category" to txn.category.trim(), "description" to txn.description.trim(), "transactionDate" to txn.transactionDate, "createdBy" to actor, "createdAt" to FieldValue.serverTimestamp(), "updatedAt" to FieldValue.serverTimestamp(), "memberId" to txn.memberId, "duesPaymentId" to txn.duesPaymentId, "note" to txn.note?.trim(), "archived" to false))
            batch.set(summaryRef, mapOf("income" to FieldValue.increment(incomeDelta), "expense" to FieldValue.increment(expenseDelta)), com.google.firebase.firestore.SetOptions.merge())
            batch.set(db.collection("auditLogs").document(), mapOf("actorUid" to actor, "action" to if (txn.id.isBlank()) "TRANSACTION_CREATED" else "TRANSACTION_EDITED", "entityType" to "TRANSACTION", "entityId" to id, "timestamp" to FieldValue.serverTimestamp(), "summary" to "${txn.category}: ${txn.amount}"))
        }.await()
    }

    suspend fun recordDuesPayment(periodId: String, memberId: String, amount: Long, paymentDate: Long, note: String?): AppResult<Unit> = result {
        val actor = uid() ?: error("Sesi berakhir")
        val paymentId = "${periodId}_$memberId"
        val paymentRef = db.collection("duesPayments").document(paymentId)
        val transactionRef = db.collection("transactions").document("dues_$paymentId")
        db.runTransaction { transaction ->
            val existing = transaction.get(paymentRef)
            check(!existing.exists() || existing.getString("status") != DuesStatus.PAID.name) { "Pembayaran bulan ini sudah tercatat." }
            transaction.set(paymentRef, mapOf("memberId" to memberId, "periodId" to periodId, "amount" to amount, "paymentDate" to paymentDate, "status" to DuesStatus.PAID.name, "transactionId" to transactionRef.id, "note" to note?.trim(), "createdBy" to actor, "createdAt" to FieldValue.serverTimestamp(), "updatedAt" to FieldValue.serverTimestamp()))
            transaction.set(transactionRef, mapOf("type" to TransactionType.INCOME.name, "amount" to amount, "category" to "Iuran", "description" to "Iuran $periodId", "transactionDate" to paymentDate, "createdBy" to actor, "createdAt" to FieldValue.serverTimestamp(), "updatedAt" to FieldValue.serverTimestamp(), "duesPaymentId" to paymentId, "archived" to false))
            transaction.set(db.collection("auditLogs").document(), mapOf("actorUid" to actor, "action" to "DUES_PAYMENT_RECORDED", "entityType" to "DUES_PAYMENT", "entityId" to paymentId, "timestamp" to FieldValue.serverTimestamp(), "summary" to "Pembayaran iuran"))
        }.await()
    }

    suspend fun saveMember(member: Member): AppResult<Unit> = result {
        val actor = uid() ?: error("Sesi berakhir"); val ref = db.collection("members").document(member.id.ifBlank { UUID.randomUUID().toString() })
        db.runBatch { batch -> batch.set(ref, mapOf("fullName" to member.fullName.trim(), "normalizedName" to member.fullName.trim().lowercase(), "phoneNumber" to member.phoneNumber?.trim()?.ifBlank { null }, "status" to member.status.name, "createdAt" to FieldValue.serverTimestamp(), "updatedAt" to FieldValue.serverTimestamp()), com.google.firebase.firestore.SetOptions.merge()); batch.set(db.collection("auditLogs").document(), mapOf("actorUid" to actor, "action" to "MEMBER_SAVED", "entityType" to "MEMBER", "entityId" to ref.id, "timestamp" to FieldValue.serverTimestamp(), "summary" to member.fullName.trim())) }.await()
    }

    private suspend fun <T> result(block: suspend () -> T): AppResult<T> = runCatching { block() }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage()) })
    private suspend fun <T> page(query: Query, limit: Long, cursor: Any?, mapper: (com.google.firebase.firestore.DocumentSnapshot) -> T): AppResult<Page<T>> = result { val safeLimit = limit.coerceIn(1, 100); var q = query.limit(safeLimit + 1); if (cursor is com.google.firebase.firestore.DocumentSnapshot) q = q.startAfter(cursor); val snap = q.get().await(); val docs = snap.documents.take(safeLimit.toInt()); Page(docs.map(mapper).distinctBy { it.hashCode() }, snap.size() > safeLimit, docs.lastOrNull()) }
    private fun <T> AppResult<Page<T>>.mapItems(filter: (List<T>) -> List<T>) = when (this) { is AppResult.Success -> AppResult.Success(data.copy(items = filter(data.items))); is AppResult.Failure -> this }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toTransaction() = Transaction(id, runCatching { TransactionType.valueOf(getString("type").orEmpty()) }.getOrDefault(TransactionType.INCOME), getLong("amount") ?: 0, getString("category").orEmpty(), getString("description").orEmpty(), getLong("transactionDate") ?: 0, getString("createdBy").orEmpty(), getTimestamp("createdAt")?.toDate()?.time ?: 0, getTimestamp("updatedAt")?.toDate()?.time ?: 0, getString("memberId"), getString("duesPaymentId"), getString("note"))
private fun com.google.firebase.firestore.DocumentSnapshot.toMember() = Member(id, getString("fullName").orEmpty(), getString("normalizedName").orEmpty(), getString("phoneNumber"), runCatching { MemberStatus.valueOf(getString("status").orEmpty()) }.getOrDefault(MemberStatus.ACTIVE))
private fun com.google.firebase.firestore.DocumentSnapshot.toDuesPayment() = DuesPayment(id, getString("memberId").orEmpty(), getString("periodId").orEmpty(), getLong("amount") ?: 0, getLong("paymentDate"), runCatching { DuesStatus.valueOf(getString("status").orEmpty()) }.getOrDefault(DuesStatus.UNPAID), getString("transactionId"), getString("note"))
private fun com.google.firebase.firestore.DocumentSnapshot.toDuesPeriod() = DuesPeriod(id, getLong("year")?.toInt() ?: 0, getLong("month")?.toInt() ?: 0, getLong("defaultAmount") ?: 0, getBoolean("active") ?: true)
