package com.jki.myhananeelcinta.auth

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.jki.myhananeelcinta.BuildConfig
import com.jki.myhananeelcinta.model.User
import com.jki.myhananeelcinta.util.UserConfiguration
import java.io.File
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class CmsAuthRepository private constructor(context: Context) {
    private val queue = Volley.newRequestQueue(context.applicationContext)
    private val auth = FirebaseAuth.getInstance()
    private var retryAt = 0L
    private var rateLimitCount = 0
    private val bridge = CmsAuthBridge(::getToken) { path, body, token, done -> send(path, body, token, done) }

    companion object {
        @Volatile private var instance: CmsAuthRepository? = null
        fun getInstance(context: Context): CmsAuthRepository = instance ?: synchronized(this) {
            instance ?: CmsAuthRepository(context).also { instance = it }
        }
    }

    fun openSession(callback: (Result<User>) -> Unit) = bridge.openSession(callback)
    fun register(body: JsonObject, photo: File? = null, callback: (Result<User>) -> Unit) {
        // Bind the selected photo to this registration, including the single 401 retry.
        val registrationBridge = CmsAuthBridge(::getToken) { path, fields, token, done ->
            send(path, fields, token, done, photo.takeIf { path == "/auth/register" })
        }
        registrationBridge.register(body, callback)
    }

    fun saveSession(user: User) {
        UserConfiguration.getInstance().setUserId(user.id)
        UserConfiguration.getInstance().setUserData(user)
    }

    fun handleFailure(error: Throwable) {
        UserConfiguration.getInstance().clearSession()
        if ((error as? CmsAuthException)?.status == 401) auth.signOut()
    }

    private fun getToken(forceRefresh: Boolean, callback: (Result<String>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(Result.failure(CmsAuthException(401)))
            return
        }
        currentUser.getIdToken(forceRefresh).addOnCompleteListener { task ->
            val token = if (task.isSuccessful) task.result?.token else null
            when {
                auth.currentUser?.uid != currentUser.uid -> callback(Result.failure(CmsAuthException(401)))
                task.isSuccessful && !token.isNullOrBlank() -> callback(Result.success(token))
                task.exception is FirebaseAuthInvalidUserException -> callback(Result.failure(CmsAuthException(401)))
                else -> callback(Result.failure(CmsAuthException(detail = "Gagal mengambil token Firebase. Periksa koneksi lalu coba lagi.")))
            }
        }
    }

    private fun send(path: String, body: JsonObject?, token: String, callback: (Result<JsonObject>) -> Unit, photo: File? = null) {
        val now = System.currentTimeMillis()
        if (retryAt > now) {
            callback(Result.failure(CmsAuthException(429, retryAfterSeconds = (retryAt - now + 999) / 1000)))
            return
        }
        val boundary = "hananeel-${UUID.randomUUID()}"
        val contentType = if (photo == null) "application/json; charset=utf-8" else "multipart/form-data; boundary=$boundary"
        val encoded = runCatching {
            if (photo == null) body?.toString()?.toByteArray(Charsets.UTF_8)
            else CmsRegistrationMultipart.encode(requireNotNull(body), photo, boundary)
        }
        if (encoded.isFailure) {
            callback(Result.failure(CmsAuthException(detail = "Foto tidak dapat dibaca. Silakan ambil ulang foto.")))
            return
        }
        val uid = auth.currentUser?.uid
        val request = object : StringRequest(if (body == null) Request.Method.GET else Request.Method.POST,
            BuildConfig.CMS_API_BASE_URL.trimEnd('/') + path,
            Response.Listener { response ->
                if (auth.currentUser?.uid != uid || uid == null) {
                    callback(Result.failure(CmsAuthException(401)))
                } else {
                    val parsed = runCatching {
                        val json = JsonParser.parseString(response).asJsonObject
                        require(json.getAsJsonObject("data")?.getAsJsonObject("account")?.get("uid")?.asString == uid)
                        json
                    }
                    rateLimitCount = 0
                    callback(parsed.fold({ Result.success(it) }, { Result.failure(CmsAuthException(detail = "Response CMS tidak valid. Silakan coba lagi.")) }))
                }
            }, Response.ErrorListener { error ->
                val status = error.networkResponse?.statusCode ?: 0
                val detail = runCatching {
                    val json = JsonParser.parseString(String(error.networkResponse?.data ?: byteArrayOf(), Charsets.UTF_8)).asJsonObject
                    json.getAsJsonObject("errors")?.entrySet()?.flatMap { entry ->
                        entry.value.asJsonArray.map { it.asString }
                    }?.joinToString("\n").orEmpty()
                }.getOrDefault("")
                val delay = if (status == 429) {
                    rateLimitCount = (rateLimitCount + 1).coerceAtMost(6)
                    val header = error.networkResponse?.headers?.entries?.firstOrNull { it.key.equals("Retry-After", true) }?.value
                    retryDelay(header).also { retryAt = System.currentTimeMillis() + it * 1000 }
                } else 0L
                callback(Result.failure(CmsAuthException(status, detail, delay)))
            }) {
            override fun getHeaders(): MutableMap<String, String> = mutableMapOf(
                "Authorization" to "Bearer $token", "Accept" to "application/json", "Content-Type" to contentType,
                "X-App-Platform" to "android", "X-App-Version" to BuildConfig.VERSION_NAME
            )
            override fun getBodyContentType() = contentType
            override fun getBody(): ByteArray? = encoded.getOrThrow()
        }
        request.setShouldCache(false)
        // No automatic Volley POST retry: registration may already have committed.
        request.retryPolicy = DefaultRetryPolicy(20_000, 0, 1f)
        queue.add(request)
    }

    private fun retryDelay(header: String?): Long {
        header?.toLongOrNull()?.let { return it.coerceIn(1, 86400) }
        if (header != null) runCatching {
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).apply { timeZone = TimeZone.getTimeZone("GMT") }.parse(header)
        }.getOrNull()?.let { return ((it.time - System.currentTimeMillis() + 999) / 1000).coerceIn(1, 86400) }
        return (1L shl rateLimitCount).coerceAtMost(60)
    }
}
