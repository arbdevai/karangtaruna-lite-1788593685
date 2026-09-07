package id.or.karangtaruna.data

import android.app.Activity
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import id.or.karangtaruna.core.data.toUserMessage
import id.or.karangtaruna.core.model.AppResult
import id.or.karangtaruna.core.model.Role
import id.or.karangtaruna.core.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "KarangTarunaAuthRepo"

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val profile: UserProfile) : SessionState
    data class ProfileUnavailable(val message: String) : SessionState
}

class AuthRepository(private val auth: FirebaseAuth, private val db: FirebaseFirestore) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _session = MutableStateFlow<SessionState>(SessionState.Loading)
    val session: StateFlow<SessionState> = _session
    private var profileListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener {
            val user = auth.currentUser
            profileListener?.remove()
            profileListener = null
            if (user == null) {
                _session.value = SessionState.SignedOut
            } else {
                _session.value = SessionState.Loading
                scope.launch {
                    when (val result = provisionProfile(user)) {
                        is AppResult.Success -> attachProfileListener(user)
                        is AppResult.Failure -> _session.value = SessionState.ProfileUnavailable(result.message)
                    }
                }
            }
        }
    }

    suspend fun retryProfile(): AppResult<Unit> {
        val user = auth.currentUser ?: return AppResult.Failure("Sesi telah berakhir. Silakan masuk lagi.")
        return when (val result = provisionProfile(user)) {
            is AppResult.Success -> { attachProfileListener(user); result }
            is AppResult.Failure -> result
        }
    }

    private suspend fun provisionProfile(user: FirebaseUser): AppResult<Unit> = runCatching {
        val ref = db.collection("users").document(user.uid)
        val snapshot = ref.get().await()
        val role = snapshot.getString("role") ?: Role.VIEWER.name
        val active = snapshot.getBoolean("active") ?: true
        ref.set(
            mapOf(
                "displayName" to (snapshot.getString("displayName")?.ifBlank { null } ?: user.displayName?.ifBlank { null } ?: user.email?.substringBefore('@') ?: "Warga"),
                "email" to (snapshot.getString("email")?.ifBlank { null } ?: user.email?.lowercase() ?: ""),
                "role" to role,
                "active" to active,
                "createdAt" to (snapshot.getTimestamp("createdAt") ?: Timestamp.now()),
                "updatedAt" to Timestamp.now(),
            ),
            SetOptions.merge(),
        ).await()
        Unit
    }.fold({ AppResult.Success(it) }, { error ->
        Log.e(TAG, "Profile provision failed: ${error.message}")
        AppResult.Failure(error.toUserMessage("menyimpan profil"))
    })

    private fun attachProfileListener(user: FirebaseUser) {
        profileListener?.remove()
        profileListener = db.collection("users").document(user.uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Profile snapshot failed: ${error.message}")
                _session.value = SessionState.ProfileUnavailable(error.toUserMessage("memuat profil"))
            } else if (snapshot == null || !snapshot.exists()) {
                _session.value = SessionState.ProfileUnavailable("Profil belum tersimpan. Tekan Coba lagi.")
            } else {
                _session.value = SessionState.SignedIn(snapshot.toUserProfile(user))
            }
        }
    }

    suspend fun login(email: String, password: String): AppResult<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        Unit
    }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage("masuk")) })

    suspend fun register(name: String, email: String, password: String): AppResult<Unit> = runCatching {
        val user = auth.createUserWithEmailAndPassword(email.trim(), password).await().user ?: error("Akun tidak tersedia")
        db.collection("users").document(user.uid).set(
            mapOf(
                "displayName" to name.trim(), "email" to email.trim().lowercase(),
                "role" to Role.VIEWER.name, "active" to true,
                "createdAt" to Timestamp.now(), "updatedAt" to Timestamp.now(),
            ),
            SetOptions.merge(),
        ).await()
        Unit
    }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage("mendaftar")) })

    suspend fun resetPassword(email: String): AppResult<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
        Unit
    }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage("mengirim reset kata sandi")) })

    suspend fun signInWithGoogle(activity: Activity): AppResult<Unit> = try {
        val provider = OAuthProvider.newBuilder("google.com").build()
        val user = auth.startActivityForSignInWithProvider(activity, provider).await().user ?: error("Akun Google tidak tersedia")
        provisionProfile(user)
    } catch (e: Throwable) {
        AppResult.Failure(e.toUserMessage("login Google"))
    }

    fun logout() = auth.signOut()

    private fun roleOf(value: String?): Role = runCatching { Role.valueOf(value.orEmpty()) }.getOrDefault(Role.VIEWER)

    private fun com.google.firebase.firestore.DocumentSnapshot.toUserProfile(user: FirebaseUser) = UserProfile(
        uid = id,
        displayName = getString("displayName")?.ifBlank { null } ?: user.displayName ?: user.email?.substringBefore('@').orEmpty(),
        email = getString("email") ?: user.email.orEmpty(),
        role = roleOf(getString("role")),
        active = getBoolean("active") ?: true,
    )
}
