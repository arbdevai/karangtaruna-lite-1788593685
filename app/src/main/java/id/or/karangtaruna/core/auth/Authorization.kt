package id.or.karangtaruna.core.auth

import id.or.karangtaruna.core.model.Role

enum class Permission { VIEW_FINANCE, MANAGE_TRANSACTIONS, VIEW_MEMBERS, MANAGE_MEMBERS, MANAGE_DUES, MANAGE_USERS, MANAGE_SETTINGS }

object RoleCapabilities {
    fun has(role: Role, permission: Permission): Boolean = when (permission) {
        Permission.VIEW_FINANCE -> true // all authenticated active users
        Permission.MANAGE_TRANSACTIONS -> role in setOf(Role.ADMIN, Role.TREASURER)
        Permission.VIEW_MEMBERS -> true
        Permission.MANAGE_MEMBERS -> role in setOf(Role.ADMIN, Role.TREASURER)
        Permission.MANAGE_DUES -> role in setOf(Role.ADMIN, Role.TREASURER)
        Permission.MANAGE_USERS -> role == Role.ADMIN
        Permission.MANAGE_SETTINGS -> role == Role.ADMIN
    }
}

object Validation {
    private val emailPattern = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)

    fun required(value: String?, label: String): String? = if (value.isNullOrBlank()) "$label wajib diisi." else null
    fun email(value: String): String? = when {
        value.isBlank() -> "Email wajib diisi."
        !emailPattern.matches(value.trim()) -> "Format email tidak valid."
        else -> null
    }
    fun password(value: String): String? = when {
        value.isBlank() -> "Kata sandi wajib diisi."
        value.length < 8 -> "Kata sandi minimal 8 karakter."
        value.none(Char::isLetter) || value.none(Char::isDigit) -> "Gunakan huruf dan angka pada kata sandi."
        else -> null
    }
    fun amount(value: Long?): String? = when {
        value == null -> "Nominal wajib diisi."
        value <= 0 -> "Nominal harus lebih dari Rp0."
        value > 1_000_000_000_000L -> "Nominal terlalu besar."
        else -> null
    }
    fun memberName(value: String): String? = when {
        value.isBlank() -> "Nama wajib diisi."
        value.trim().length < 2 -> "Nama terlalu pendek."
        value.trim().length > 80 -> "Nama terlalu panjang."
        else -> null
    }
}
