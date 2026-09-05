package id.or.karangtaruna.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import id.or.karangtaruna.core.data.toUserMessage
import id.or.karangtaruna.core.model.*
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
        auth.addAuthStateListener { user ->
            if (user == null) {
                _session.value = SessionState.SignedOut
            } else {
                val emailVal = user.email.orEmpty()
                loadProfile(user.uid, emailVal)
            }
        }
    }

    private fun loadProfile(uid: String, email: String) {
        db.collection("users").document(uid).addSnapshotListener { snapshot, error ->
            _session.value = if (error != null || snapshot == null || !snapshot.exists()) {
                SessionState.SignedIn(UserProfile(uid, email = email, displayName = email.substringBefore('@'), role = Role.VIEWER))
            } else SessionState.SignedIn(UserProfile(uid, snapshot.getString("displayName").orEmpty(), snapshot.getString("email") ?: email, roleOf(snapshot.getString("role")), snapshot.getBoolean("active") ?: true))
        }
    }

    suspend fun login(email: String, password: String): AppResult<Unit> = runCatching { auth.signInWithEmailAndPassword(email.trim(), password).await(); Unit }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage()) })
    suspend fun register(name: String, email: String, password: String): AppResult<Unit> = runCatching {
        val user = auth.createUserWithEmailAndPassword(email.trim(), password).await().user ?: error("Akun tidak tersedia")
        db.collection("users").document(user.uid).set(mapOf("displayName" to name.trim(), "email" to email.trim().lowercase(), "role" to Role.VIEWER.name, "active" to true, "createdAt" to Timestamp.now(), "updatedAt" to Timestamp.now())).await()
        Unit
    }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage()) })
    suspend fun resetPassword(email: String): AppResult<Unit> = runCatching { auth.sendPasswordResetEmail(email.trim()).await(); Unit }.fold({ AppResult.Success(it) }, { AppResult.Failure(it.toUserMessage()) })
    fun logout() = auth.signOut()
    private fun roleOf(value: String?): Role = runCatching { Role.valueOf(value.orEmpty()) }.getOrDefault(Role.VIEWER)
}
