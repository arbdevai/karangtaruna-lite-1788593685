package id.or.karangtaruna.core.model

enum class Role { ADMIN, TREASURER, VIEWER }
enum class TransactionType { INCOME, EXPENSE }
enum class DuesStatus { UNPAID, PAID }
enum class MemberStatus { ACTIVE, INACTIVE }

data class UserProfile(val uid: String = "", val displayName: String = "", val email: String = "", val role: Role = Role.VIEWER, val active: Boolean = true)
data class OrganizationSettings(val name: String = "Karang Taruna", val rtNumber: String = "", val duesDefaultAmount: Long = 0)
data class Member(val id: String = "", val fullName: String = "", val normalizedName: String = "", val phoneNumber: String? = null, val status: MemberStatus = MemberStatus.ACTIVE)
data class Transaction(val id: String = "", val type: TransactionType = TransactionType.INCOME, val amount: Long = 0, val category: String = "Lainnya", val description: String = "", val transactionDate: Long = 0, val createdBy: String = "", val createdAt: Long = 0, val updatedAt: Long = 0, val memberId: String? = null, val duesPaymentId: String? = null, val note: String? = null)
data class DuesPeriod(val id: String = "", val year: Int = 0, val month: Int = 0, val defaultAmount: Long = 0, val active: Boolean = true)
data class DuesPayment(val id: String = "", val memberId: String = "", val periodId: String = "", val amount: Long = 0, val paymentDate: Long? = null, val status: DuesStatus = DuesStatus.UNPAID, val transactionId: String? = null, val note: String? = null)
data class FinanceSummary(val balance: Long = 0, val income: Long = 0, val expense: Long = 0)
data class Page<T>(val items: List<T>, val hasMore: Boolean, val cursor: Any? = null)
sealed interface AppResult<out T> { data class Success<T>(val data: T) : AppResult<T>; data class Failure(val message: String) : AppResult<Nothing> }
