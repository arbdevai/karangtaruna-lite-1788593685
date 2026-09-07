package id.or.karangtaruna.data

import android.app.Activity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import id.or.karangtaruna.core.data.toUserMessage
import id.or.karangtaruna.core.model.AppResult
import id.or.karangtaruna.core.model.Role
import id.or.karangtaruna.core.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

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
                loadProfile(user.uid, user.email.orEmpty())
            }
        }
    }

    private fun loadProfile(uid: String, email: String) {
        db.collection("users").document(uid).addSnapshotListener { snapshot, error ->
            _session.value = if (error != null || snapshot == null || !snapshot.exists()) {
                SessionState.SignedIn(UserProfile(uid, email = email, displayName = email.substringBefore('@'), role = Role.VIEWER))
            } else {
                SessionState.SignedIn(
                    UserProfile(
                        uid = uid,
                        displayName = snapshot.getString("displayName").orEmpty(),
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
    }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage()) })

    suspend fun register(name: String, email: String, password: String): AppResult<Unit> = runCatching {
        val user = auth.createUserWithEmailAndPassword(email.trim(), password).await().user ?: error("Akun tidak tersedia")
        db.collection("users").document(user.uid).set(
            mapOf(
                "displayName" to name.trim(),
                "email" to email.trim().lowercase(),
                "role" to Role.VIEWER.name,
                "active" to true,
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now(),
            ),
        ).await()
        Unit
    }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage()) })

    suspend fun resetPassword(email: String): AppResult<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
        Unit
    }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage()) })

    suspend fun signInWithGoogle(activity: Activity): AppResult<Unit> = runCatching {
        val provider = OAuthProvider.newBuilder("google.com").build()
        val result = auth.startActivityForSignInWithProvider(activity, provider).await()
        val user = result.user ?: error("Akun Google tidak tersedia")
        val ref = db.collection("users").document(user.uid)
        if (!ref.get().await().exists()) {
            ref.set(
                mapOf(
                    "displayName" to (user.displayName ?: user.email?.substringBefore('@') ?: "Pengguna"),
                    "email" to (user.email?.lowercase() ?: ""),
                    "role" to Role.VIEWER.name,
                    "active" to true,
                    "createdAt" to Timestamp.now(),
                    "updatedAt" to Timestamp.now(),
                ),
            ).await()
        }
        Unit
    }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage()) })

    fun logout() = auth.signOut()

    private fun roleOf(value: String?): Role = runCatching { Role.valueOf(value.orEmpty()) }.getOrDefault(Role.VIEWER)
}
