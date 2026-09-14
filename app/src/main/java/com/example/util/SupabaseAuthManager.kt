package com.example.util

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class AuthUser(val uid: String, val email: String, val displayName: String,
    val phone: String = "", val photoUrl: String = "", val isAnonymous: Boolean = false,
    val provider: String = "password")

sealed class AuthResult {
    data class Success(val user: AuthUser, val message: String) : AuthResult()
    data class Error(val errorMessage: String) : AuthResult()
    data class ConfirmationRequired(val message: String) : AuthResult()
    object Loading : AuthResult()
}

/** Session tokens stay in memory, never SharedPreferences, logs, or device backups. */
object SupabaseSession {
    private val mutex = Mutex()
    @Volatile private var state: Session? = null
    private data class Session(val user: AuthUser, val access: String, val refresh: String, val expires: Long, val deadline: Long)
    val currentUser: AuthUser? get() = state?.takeIf { SystemClock.elapsedRealtime() < it.deadline }?.user
    private val http = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS).followRedirects(false).followSslRedirects(false).build()

    private fun request(path: String, payload: JSONObject? = null, bearer: String? = null): JSONObject {
        val base = BuildConfig.SUPABASE_URL.trimEnd('/')
        require(base.startsWith("https://") && BuildConfig.SUPABASE_PUBLISHABLE_KEY.isNotBlank()) {
            "Supabase configuration is missing."
        }
        val builder = Request.Builder().url("$base/auth/v1/$path")
            .header("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
        if (bearer != null) builder.header("Authorization", "Bearer $bearer")
        if (payload != null) builder.post(payload.toString().toRequestBody("application/json".toMediaType()))
        http.newCall(builder.build()).execute().use { response ->
            Log.i("CngSecurity", "auth_http status=${response.code}")
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val code = runCatching { JSONObject(body).optString("error_code") }.getOrDefault("")
                throw IOException(when {
                    code == "email_not_confirmed" -> "Confirm your email before signing in."
                    response.code == 429 -> "Too many attempts. Please wait and retry."
                    code == "invalid_credentials" -> "Email or password is incorrect."
                    response.code in 400..499 -> "Authentication request rejected. Check your details and email confirmation."
                    else -> "Authentication service unavailable. Please retry."
                })
            }
            return if (body.isBlank()) JSONObject() else JSONObject(body)
        }
    }

    private fun accept(json: JSONObject, deadline: Long = SystemClock.elapsedRealtime() + TimeUnit.HOURS.toMillis(12)): AuthUser {
        val token = json.getString("access_token")
        // Use the server-verified user endpoint, never user-controlled profile claims for identity.
        val user = request("user", bearer = token)
        require(!user.optString("email_confirmed_at").let { it.isBlank() || it == "null" }) { "Confirm your email before signing in." }
        val meta = user.optJSONObject("user_metadata") ?: JSONObject()
        val parsed = AuthUser(user.getString("id"), user.optString("email"),
            meta.optString("display_name").ifBlank { "CNG Driver" }, meta.optString("phone"))
        require(!user.optBoolean("is_anonymous", false)) { "An account is required for cloud backup." }
        state = Session(parsed, token, json.getString("refresh_token"),
            System.currentTimeMillis() + json.optLong("expires_in", 3600).coerceIn(1, 3600) * 1000, deadline)
        return parsed
    }

    suspend fun authenticate(email: String, password: String, name: String? = null, phone: String = ""): AuthResult =
        withContext(Dispatchers.IO) { mutex.withLock {
            try {
                require(email.contains("@") && password.isNotEmpty()) { "Enter your email and password." }
                if (name != null) require(password.length >= 12) { "Use a password with at least 12 characters." }
                val payload = JSONObject().put("email", email.trim()).put("password", password)
                if (name != null) payload.put("data", JSONObject().put("display_name", name.trim()).put("phone", phone))
                val json = request(if (name == null) "token?grant_type=password" else "signup", payload)
                if (!json.has("access_token") || json.isNull("access_token")) {
                    AuthResult.ConfirmationRequired("Check your email to confirm the account, then sign in.")
                } else AuthResult.Success(accept(json), "Signed in securely with Supabase.")
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { AuthResult.Error(e.message ?: "Sign-in failed. Please retry.") }
        } }

    suspend fun credentials(): Pair<String, String> = withContext(Dispatchers.IO) { mutex.withLock {
        var session = state ?: error("Sign in to your Supabase account first.")
        if (SystemClock.elapsedRealtime() >= session.deadline) {
            state = null
            error("Session expired. Please sign in again.")
        }
        if (session.expires <= System.currentTimeMillis() + 60000) {
            try {
                accept(request("token?grant_type=refresh_token", JSONObject().put("refresh_token", session.refresh)), session.deadline)
                session = state ?: error("Please sign in again.")
            } catch (e: Exception) {
                state = null
                throw e
            }
        }
        session.user.uid to session.access
    } }

    suspend fun signOut(): String = withContext(Dispatchers.IO) { mutex.withLock {
        val previous = state
        state = null
        if (previous == null) return@withLock "Signed out."
        try {
            request("logout?scope=local", JSONObject(), previous.access)
            "Signed out."
        } catch (e: CancellationException) { throw e }
        catch (_: Exception) { "Signed out on this device. Server logout could not be confirmed; retry when online." }
    } }
}

class SupabaseAuthManager {
    val currentUser: AuthUser? get() = SupabaseSession.currentUser
    val isUserLoggedIn: Boolean get() = currentUser != null
    suspend fun signInWithEmailAndPassword(email: String, password: String) = SupabaseSession.authenticate(email, password)
    suspend fun createUserWithEmailAndPassword(email: String, password: String, displayName: String, phone: String = "") =
        SupabaseSession.authenticate(email, password, displayName, phone)
    suspend fun signInWithGoogle(context: Context): AuthResult =
        AuthResult.Error("Google sign-in is not configured. Use email and password.")
    suspend fun sendPasswordResetEmail(email: String): Result<String> =
        Result.failure(IllegalStateException("Password recovery is not configured in this build. Contact the app owner."))
    suspend fun signOut(context: Context? = null): String = SupabaseSession.signOut()
}
