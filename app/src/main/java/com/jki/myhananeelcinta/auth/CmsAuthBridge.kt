package com.jki.myhananeelcinta.auth

import com.google.gson.JsonObject
import com.jki.myhananeelcinta.model.User

class CmsAuthException(
    val status: Int = 0,
    val detail: String = "",
    val retryAfterSeconds: Long = 0
) : Exception(when (status) {
    401 -> "Sesi Firebase tidak valid. Silakan login ulang."
    403 -> "Profil belum terhubung ke CMS atau akun nonaktif. Hubungi admin gereja."
    409 -> "Akun atau email sudah terdaftar di CMS. Hubungi admin gereja untuk menghubungkan akun."
    422 -> detail.ifBlank { "Data registrasi tidak valid. Periksa kembali formulir." }
    429 -> "Terlalu banyak percobaan. Coba lagi dalam ${retryAfterSeconds.coerceAtLeast(1)} detik."
    503 -> "Verifikasi Firebase di CMS sedang tidak tersedia. Silakan coba lagi."
    0 -> detail.ifBlank { "Tidak dapat menghubungi CMS. Periksa koneksi lalu coba lagi." }
    else -> "CMS sedang bermasalah. Silakan coba lagi."
})

/** Transport and token source are injected so recovery can be tested without Firebase or a server. */
class CmsAuthBridge(
    private val tokenSource: (Boolean, (Result<String>) -> Unit) -> Unit,
    private val transport: (String, JsonObject?, String, (Result<JsonObject>) -> Unit) -> Unit
) {
    fun openSession(callback: (Result<User>) -> Unit) = request("/auth/session", JsonObject(), callback = callback)
    fun getProfile(callback: (Result<User>) -> Unit) = request("/me", null, callback = callback)

    fun register(body: JsonObject, callback: (Result<User>) -> Unit) {
        request("/auth/register", body) { result ->
            val error = result.exceptionOrNull() as? CmsAuthException
            if (error != null && (error.status == 409 || error.status == 0 || error.status >= 500)) {
                // A timeout may happen after commit. Never create another Firebase account or overwrite the profile.
                getProfile { recovered ->
                    if (recovered.isSuccess) callback(recovered)
                    else callback(Result.failure(recovered.exceptionOrNull()
                        ?.takeIf { (it as? CmsAuthException)?.status == 401 } ?: error))
                }
            } else callback(result)
        }
    }

    private fun request(path: String, body: JsonObject?, refreshed: Boolean = false, callback: (Result<User>) -> Unit) {
        tokenSource(refreshed) { tokenResult ->
            tokenResult.fold({ token ->
                transport(path, body, token) { response ->
                    val error = response.exceptionOrNull() as? CmsAuthException
                    if (error?.status == 401 && !refreshed) request(path, body, true, callback)
                    else callback(response.mapCatching { CmsProfileMapper.session(it) })
                }
            }, { callback(Result.failure(it)) })
        }
    }
}
