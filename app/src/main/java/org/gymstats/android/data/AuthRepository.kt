package org.gymstats.android.data
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
class AuthRepository(private val auth: FirebaseAuth) {
    val currentUser get() = auth.currentUser
    suspend fun login(email: String, password: String) { auth.signInWithEmailAndPassword(email.trim(), password).await() }
    suspend fun register(email: String, password: String) { auth.createUserWithEmailAndPassword(email.trim(), password).await() }
    suspend fun google(token: String) { auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await() }
    suspend fun reset(email: String) { auth.sendPasswordResetEmail(email.trim()).await() }
    fun logout() = auth.signOut()
}
