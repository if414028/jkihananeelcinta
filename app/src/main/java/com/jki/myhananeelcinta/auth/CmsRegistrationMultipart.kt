package com.jki.myhananeelcinta.auth

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.ByteArrayOutputStream
import java.io.File

/** Laravel-compatible form fields plus a JPEG file; no Firebase Storage upload. */
object CmsRegistrationMultipart {
    fun encode(fields: JsonObject, photo: File, boundary: String): ByteArray {
        require(photo.isFile && photo.length() in 1..5_242_880) { "Foto tidak valid." }
        val output = ByteArrayOutputStream()
        fun write(text: String) = output.write(text.toByteArray(Charsets.UTF_8))
        fun field(name: String, value: JsonElement) {
            if (value.isJsonNull) return
            require(name.matches(Regex("[a-z_]+(\\[\\])?")))
            write("--$boundary\r\nContent-Disposition: form-data; name=\"$name\"\r\n\r\n")
            val primitive = value.asJsonPrimitive
            write(if (primitive.isBoolean) { if (primitive.asBoolean) "1" else "0" } else primitive.asString)
            write("\r\n")
        }
        fields.entrySet().forEach { (key, value) ->
            if (value.isJsonArray) value.asJsonArray.forEach { field("$key[]", it) }
            else field(key, value)
        }
        write("--$boundary\r\nContent-Disposition: form-data; name=\"profile_photo\"; filename=\"profile.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n")
        photo.inputStream().use { it.copyTo(output) }
        write("\r\n--$boundary--\r\n")
        return output.toByteArray()
    }
}
