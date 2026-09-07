package id.or.karangtaruna.core.data

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException

private const val AUTH_TAG = "KarangTarunaAuth"

fun Throwable.toUserMessage(operation: String = "auth"): String {
    val code = when (this) {
        is FirebaseAuthException -> errorCode
        else -> null
    }
    Log.w(AUTH_TAG, "$operation failed: class=${this::class.java.simpleName}, code=${code ?: "n/a"}, message=${message ?: "n/a"}")
    return when (code) {
        "ERROR_EMAIL_ALREADY_IN_USE" -> "Email ini sudah terdaftar."
        "ERROR_INVALID_EMAIL" -> "Format email tidak valid."
        "ERROR_WEAK_PASSWORD" -> "Kata sandi terlalu lemah. Gunakan minimal 8 karakter dengan huruf dan angka."
        "ERROR_NETWORK_REQUEST_FAILED" -> "Tidak dapat terhubung ke server. Periksa koneksi internet."
        "ERROR_TOO_MANY_REQUESTS" -> "Terlalu banyak percobaan. Coba lagi nanti."
        "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Email atau kata sandi tidak cocok."
        "ERROR_USER_DISABLED" -> "Akun ini telah dinonaktifkan."
        "ERROR_OPERATION_NOT_ALLOWED" -> "Metode masuk ini belum diaktifkan di Firebase."
        "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "Email sudah terhubung dengan metode masuk lain."
        "ERROR_CREDENTIAL_ALREADY_IN_USE" -> "Akun Google sudah digunakan akun lain."
        "ERROR_WEB_CONTEXT_CANCELED", "ERROR_CANCELED" -> "Login Google dibatalkan."
        "ERROR_WEB_INTERNAL_ERROR", "ERROR_WEB_STORAGE_UNSUPPORTED" -> "Login Google belum siap di perangkat ini."
        else -> when (this) {
            is FirebaseNetworkException -> "Tidak dapat terhubung ke server. Periksa koneksi internet."
            is FirebaseFirestoreException -> when (this.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Anda tidak memiliki izin untuk membaca atau mengubah data ini."
                FirebaseFirestoreException.Code.UNAVAILABLE -> "Layanan sedang sibuk. Coba sesaat lagi."
                FirebaseFirestoreException.Code.FAILED_PRECONDITION -> "Data belum siap. Periksa index Firestore atau coba lagi."
                FirebaseFirestoreException.Code.NOT_FOUND -> "Data tidak ditemukan."
                else -> "Data gagal dimuat. Coba lagi."
            }
            else -> "Terjadi kesalahan saat $operation. Coba lagi."
        }
    }
}
