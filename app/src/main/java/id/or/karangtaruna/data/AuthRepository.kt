package id.or.karangtaruna.data

import android.app.Activity
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import id.or.karangtaruna.core.data.toUserMessage
import id.or.karangtaruna.core.model.AppResult
import id.or.karangtaruna.core.model.Role
import id.or.karangtaruna.core.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "KarangTarunaAuthRepo"

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val profile: UserProfile) : SessionState
}

class AuthRepository(private val auth: FirebaseAuth, private val db: FirebaseFirestore) {
    private val _session = MutableStateFlow<SessionState>(SessionState.Loading)
    val session: StateFlow<SessionState> = _session

    init {
        auth.addAuthStateListener {
            val user = auth.currentUser
            if (user == null) {
                _session.value = SessionState.SignedOut
            } else {
                ensureProfile(user.uid, user.displayName, user.email)
                loadProfile(user.uid, user.email.orEmpty())
            }
        }
    }

    private fun ensureProfile(uid: String, displayName: String?, email: String?) {
        val ref = db.collection("users").document(uid)
        ref.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                ref.set(
                    mapOf(
                        "displayName" to (displayName?.ifBlank { null } ?: email?.substringBefore('@') ?: "Warga"),
                        "email" to (email?.lowercase() ?: ""),
                        "role" to Role.VIEWER.name,
                        "active" to true,
                        "createdAt" to Timestamp.now(),
                        "updatedAt" to Timestamp.now(),
                    ),
                    SetOptions.merge(),
                ).addOnFailureListener { error -> Log.e(TAG, "Profile bootstrap failed: ${error.message}") }
            }
        }.addOnFailureListener { error -> Log.e(TAG, "Profile lookup failed: ${error.message}") }
    }

    private fun loadProfile(uid: String, email: String) {
        db.collection("users").document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Snapshot error for user $uid: ${error.message}")
            }
            _session.value = if (error != null || snapshot == null || !snapshot.exists()) {
                SessionState.SignedIn(UserProfile(uid, email = email, displayName = email.substringBefore('@').ifBlank { "Warga" }, role = Role.VIEWER))
            } else {
                SessionState.SignedIn(
                    UserProfile(
                        uid = uid,
                        displayName = snapshot.getString("displayName")?.ifBlank { email.substringBefore('@') } ?: "Warga",
                        email = snapshot.getString("email") ?: email,
                        role = roleOf(snapshot.getString("role")),
                        active = snapshot.getBoolean("active") ?: true,
                    ),
                )
            }
        }
    }

    suspend fun login(email: String, password: String): AppResult<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        Unit
    }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage("masuk")) })

    suspend fun register(name: String, email: String, password: String): AppResult<Unit> = runCatching {
        val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = authResult.user ?: error("Akun tidak tersedia")
        val profileData = mapOf(
            "displayName" to name.trim(),
            "email" to email.trim().lowercase(),
            "role" to Role.VIEWER.name,
            "active" to true,
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now(),
        )
        runCatching {
            db.collection("users").document(user.uid).set(profileData, SetOptions.merge()).await()
        }.onFailure { profileError ->
            Log.e(TAG, "Profile creation failed after successful registration: ${profileError.message}")
        }
        Unit
    }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage("mendaftar")) })

    suspend fun resetPassword(email: String): AppResult<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
        Unit
    }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage("mengirim reset kata sandi")) })

    suspend fun signInWithGoogle(activity: Activity): AppResult<Unit> = runCatching {
        val provider = OAuthProvider.newBuilder("google.com").build()
        val result = auth.startActivityForSignInWithProvider(activity, provider).await()
        val user = result.user ?: error("Akun Google tidak tersedia")
        val userRef = db.collection("users").document(user.uid)
        runCatching {
            val snapshot = userRef.get().await()
            if (!snapshot.exists()) {
                userRef.set(
                    mapOf(
                        "displayName" to (user.displayName?.ifBlank { null } ?: user.email?.substringBefore('@') ?: "Pengguna"),
                        "email" to (user.email?.lowercase() ?: ""),
                        "role" to Role.VIEWER.name,
                        "active" to true,
                        "createdAt" to Timestamp.now(),
                        "updatedAt" to Timestamp.now(),
                    ),
                    SetOptions.merge(),
                ).await()
            }
        }.onFailure { profileError ->
            Log.e(TAG, "Google profile creation failed: ${profileError.message}")
        }
        Unit
    }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage("login Google")) })

    fun logout() = auth.signOut()

    private fun roleOf(value: String?): Role = runCatching { Role.valueOf(value.orEmpty()) }.getOrDefault(Role.VIEWER)
}
