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
                    provisionProfileOrAttach(user)
                }
            }
        }
    }

    private suspend fun provisionProfileOrAttach(user: FirebaseUser) {
        // Ensure user document exists without blocking on transient read errors
        val ref = db.collection("users").document(user.uid)
        val existing = try { ref.get().await() } catch (e: Exception) { Log.w(TAG, "Profile read failed: ${e.message}"); null }
        if (existing != null && !existing.exists()) {
            runCatching {
                ref.set(
                    mapOf(
                        "displayName" to (user.displayName?.ifBlank { null } ?: user.email?.substringBefore('@') ?: "Warga"),
                        "email" to (user.email?.lowercase() ?: ""),
                        "role" to Role.VIEWER.name,
                        "active" to true,
                        "createdAt" to Timestamp.now(),
                        "updatedAt" to Timestamp.now(),
                    ),
                    SetOptions.merge(),
                ).await()
            }.onFailure { error -> Log.w(TAG, "Profile bootstrap write failed: ${error.message}") }
        }
        attachProfileListener(user)
    }

    fun retryProfile() {
        val user = auth.currentUser ?: return
        _session.value = SessionState.Loading
        scope.launch { provisionProfileOrAttach(user) }
    }

    private fun attachProfileListener(user: FirebaseUser) {
        profileListener?.remove()
        profileListener = db.collection("users").document(user.uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Profile snapshot failed: ${error.message}")
                // Recover with explicit defaults so no UI shows empty role/status
                _session.value = SessionState.SignedIn(
                    UserProfile(uid = user.uid, displayName = user.displayName ?: user.email?.substringBefore('@').orEmpty(), email = user.email.orEmpty(), role = Role.VIEWER, active = true),
                )
            } else if (snapshot == null || !snapshot.exists()) {
                _session.value = SessionState.SignedIn(
                    UserProfile(uid = user.uid, displayName = user.displayName ?: user.email?.substringBefore('@').orEmpty(), email = user.email.orEmpty(), role = Role.VIEWER, active = true),
                )
            } else {
                _session.value = SessionState.SignedIn(snapshot.toSafeProfile(user))
            }
        }
    }

    suspend fun login(email: String, password: String): AppResult<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        Unit
    }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage("masuk")) })

    suspend fun register(name: String, email: String, password: String): AppResult<Unit> = runCatching {
        val user = auth.createUserWithEmailAndPassword(email.trim(), password).await().user ?: error("Akun tidak tersedia")
        runCatching {
            db.collection("users").document(user.uid).set(
                mapOf("displayName" to name.trim(), "email" to email.trim().lowercase(), "role" to Role.VIEWER.name, "active" to true, "createdAt" to Timestamp.now(), "updatedAt" to Timestamp.now()),
                SetOptions.merge(),
            ).await()
        }
        Unit
    }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage("mendaftar")) })

    suspend fun resetPassword(email: String): AppResult<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
        Unit
    }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage("mengirim reset kata sandi")) })

    suspend fun signInWithGoogle(activity: Activity): AppResult<Unit> = runCatching {
        val provider = OAuthProvider.newBuilder("google.com").build()
        auth.startActivityForSignInWithProvider(activity, provider).await()
        Unit
    }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage("login Google")) })

    fun logout() = auth.signOut()

    private fun roleOf(value: String?): Role = runCatching { Role.valueOf(value.orEmpty()) }.getOrDefault(Role.VIEWER)

    private fun com.google.firebase.firestore.DocumentSnapshot.toSafeProfile(user: FirebaseUser) = UserProfile(
        uid = id,
        displayName = getString("displayName")?.ifBlank { null } ?: user.displayName?.ifBlank { null } ?: user.email?.substringBefore('@').orEmpty().ifBlank { "Warga" },
        email = getString("email")?.ifBlank { null } ?: user.email.orEmpty(),
        role = roleOf(getString("role")),
        active = getBoolean("active") ?: true,
    )
}
