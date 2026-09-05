package id.or.karangtaruna.core.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException

fun Throwable.toUserMessage(): String = when (this) {
    is FirebaseNetworkException -> "Tidak ada koneksi. Periksa internet lalu coba lagi."
    is FirebaseFirestoreException -> when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Anda tidak memiliki izin untuk tindakan ini."
        FirebaseFirestoreException.Code.UNAVAILABLE -> "Layanan sedang sibuk. Coba sesaat lagi."
        FirebaseFirestoreException.Code.NOT_FOUND -> "Data tidak ditemukan."
        else -> "Gagal memproses data. Coba lagi."
    }
    is FirebaseAuthException -> when (errorCode) {
        "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Email atau kata sandi tidak cocok."
        "ERROR_USER_DISABLED" -> "Akun ini telah dinonaktifkan."
        "ERROR_EMAIL_ALREADY_IN_USE" -> "Email sudah terdaftar."
        "ERROR_NETWORK_REQUEST_FAILED" -> "Tidak ada koneksi internet."
        else -> "Autentikasi gagal. Coba lagi."
    }
    else -> localizedMessage ?: "Terjadi kendala. Coba lagi."
}
