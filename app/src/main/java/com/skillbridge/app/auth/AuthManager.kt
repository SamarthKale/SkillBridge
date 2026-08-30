package com.skillbridge.app.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

/**
 * Thin wrapper around FirebaseAuth. The only thing Firebase is used for in this app is
 * email/password sign-up, login, logout and "is someone logged in".
 */
object AuthManager {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    fun currentUser(): FirebaseUser? = auth.currentUser

    fun isLoggedIn(): Boolean = currentUser() != null

    fun uid(): String = currentUser()?.uid ?: ""

    fun email(): String = currentUser()?.email ?: ""

    /** Display name if set, otherwise the part of the email before the "@". */
    fun displayName(): String {
        val name = currentUser()?.displayName
        if (!name.isNullOrBlank()) return name
        val mail = email()
        return if (mail.contains("@")) mail.substringBefore("@") else "there"
    }

    /**
     * Create an account, then save the display name on the profile.
     * [onResult] is called with a friendly error string, or null on success.
     */
    fun signUp(name: String, email: String, password: String, onResult: (String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val update = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.trim())
                    .build()
                result.user?.updateProfile(update)
                    ?.addOnCompleteListener { onResult(null) }
                    ?: onResult(null)
            }
            .addOnFailureListener { onResult(friendlyError(it)) }
    }

    /** Sign in. [onResult] gets a friendly error string, or null on success. */
    fun login(email: String, password: String, onResult: (String?) -> Unit) {
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { onResult(null) }
            .addOnFailureListener { onResult(friendlyError(it)) }
    }

    fun logout() = auth.signOut()

    /** Turn a Firebase exception into a short message a user can understand. */
    fun friendlyError(e: Throwable): String {
        val code = (e as? FirebaseAuthException)?.errorCode
        return when (code) {
            "ERROR_INVALID_EMAIL" -> "That email address looks invalid."
            "ERROR_WRONG_PASSWORD" -> "Wrong password. Please try again."
            "ERROR_USER_NOT_FOUND" -> "No account found for that email."
            "ERROR_INVALID_CREDENTIAL" -> "Email or password is incorrect."
            "ERROR_USER_DISABLED" -> "This account has been disabled."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "An account already exists for that email."
            "ERROR_WEAK_PASSWORD" -> "Password should be at least 6 characters."
            "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Check your connection."
            "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Try again later."
            else -> e.localizedMessage ?: "Something went wrong. Please try again."
        }
    }
}
