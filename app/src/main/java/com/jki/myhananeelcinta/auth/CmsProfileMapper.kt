package com.jki.myhananeelcinta.auth

import com.google.gson.JsonObject
import com.google.gson.JsonArray
import com.jki.myhananeelcinta.model.Gender
import com.jki.myhananeelcinta.model.Role
import com.jki.myhananeelcinta.model.User
import java.text.SimpleDateFormat
import java.util.Locale

/** Registration preserves legacy profile fields; credentials and admin fields stay out. */
object CmsProfileMapper {
    fun registration(user: User): JsonObject = JsonObject().apply {
        require(user.fullName.trim().length in 1..255) { "Nama lengkap wajib diisi (maksimal 255 karakter)." }
        addProperty("full_name", user.fullName.trim())
        addProperty("gender", when (user.gender) {
            Gender.MALE.gender, "male" -> "male"
            Gender.FEMALE.gender, "female" -> "female"
            else -> throw IllegalArgumentException("Pilih jenis kelamin.")
        })
        optional("nickname", user.username, 100)
        optional("place_of_birth", user.placeOfBirth, 100)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date())
        if (user.dateOfBirth.isNotBlank()) {
            val birthDate = apiDate(user.dateOfBirth)
            require(birthDate < today) { "Tanggal lahir harus sebelum hari ini." }
            addProperty("date_of_birth", birthDate)
        }
        if (user.phoneNumber.isNotBlank()) {
            require(user.phoneNumber.length in 7..30 && user.phoneNumber.matches(Regex("[0-9+() .-]+"))) {
                "Nomor telepon harus 7–30 karakter angka atau simbol telepon."
            }
            addProperty("phone_number", user.phoneNumber.trim())
        }
        optional("address", user.address, 2000)
        optional("occupation", user.job, 150)
        optional("blood_type", user.bloodType, 3)
        optional("last_education", user.lastEducation, 100)
        optional("baptism_church", user.waterBaptisteryChurch, 255)
        addProperty("holy_spirit_baptism", user.holySpiritBaptism)
        optional("church_origin", user.churchOrigin, 255)
        optional("reason_to_move_church", user.reasonToMovingChurch, 2000)
        optional("family_status", user.statusInFamily, 100)
        optional("wife_name", user.wifeName, 255)
        optional("husband_name", user.husbandName, 255)
        names("children_names", user.childrenName)
        names("siblings_names", user.siblingsName)
        val maritalStatus = user.maritalStatus.takeIf {
            it in listOf("single", "married", "widowed", "divorced")
        } ?: if (user.married) "married" else null
        if (maritalStatus != null) addProperty("marital_status", maritalStatus)
        addProperty("baptism_status", user.baptismStatus.takeIf {
            it in listOf("unknown", "not_baptized", "baptized")
        } ?: if (user.waterBaptism) "baptized" else "not_baptized")
        if (user.waterBaptism) {
            require(user.waterBaptisteryDate.isNotBlank()) { "Tanggal baptis wajib diisi jika sudah dibaptis." }
            val baptismDate = apiDate(user.waterBaptisteryDate)
            require(baptismDate <= today) { "Tanggal baptis tidak boleh di masa depan." }
            addProperty("baptism_date", baptismDate)
        }
    }

    fun session(response: JsonObject): User {
        require(response.get("success")?.asBoolean == true) { "Response CMS tidak valid." }
        val data = response.getAsJsonObject("data") ?: error("Data CMS tidak tersedia.")
        val account = data.getAsJsonObject("account") ?: error("Identitas CMS tidak tersedia.")
        val profile = data.getAsJsonObject("profile") ?: error("Profil CMS tidak tersedia.")
        require(account.text("uid").isNotBlank() && profile.text("id").isNotBlank()) { "Identitas CMS tidak valid." }
        if (profile.get("is_active")?.asBoolean != true) throw CmsAuthException(403)
        return User().apply {
            id = account.text("uid") // Existing screens still use Firebase UID as the local identity.
            email = profile.text("email")
            username = profile.text("nickname")
            nij = profile.text("member_number")
            fullName = profile.text("full_name")
            gender = when (profile.text("gender")) {
                "male" -> Gender.MALE.gender
                "female" -> Gender.FEMALE.gender
                else -> ""
            }
            placeOfBirth = profile.text("place_of_birth")
            dateOfBirth = displayDate(profile.text("date_of_birth"))
            phoneNumber = profile.text("phone_number")
            address = profile.getAsJsonObject("address")?.let { address ->
                listOf("street", "city", "province", "postal_code").map { address.text(it) }
                    .filter { it.isNotBlank() }.joinToString(", ")
            }.orEmpty()
            job = profile.text("occupation")
            married = profile.text("marital_status") == "married"
            maritalStatus = profile.text("marital_status")
            waterBaptism = profile.text("baptism_status") == "baptized"
            baptismStatus = profile.text("baptism_status")
            waterBaptisteryDate = displayDate(profile.text("baptism_date"))
            photoImageUrl = profile.text("profile_photo_url")
            bloodType = profile.text("blood_type")
            lastEducation = profile.text("last_education")
            waterBaptisteryChurch = profile.text("baptism_church")
            holySpiritBaptism = profile.get("holy_spirit_baptism")?.takeUnless { it.isJsonNull }?.asBoolean == true
            churchOrigin = profile.text("church_origin")
            reasonToMovingChurch = profile.text("reason_to_move_church")
            statusInFamily = profile.text("family_status")
            wifeName = profile.text("wife_name")
            husbandName = profile.text("husband_name")
            childrenName = profile.namesText("children_names")
            siblingsName = profile.namesText("siblings_names")
            // Only the CMS profile can grant admin menus; keep the legacy member role locally.
            role = when (profile.text("role")) {
                Role.SUPERUSER.role -> Role.SUPERUSER.role
                else -> Role.JEMAAT.role
            }
        }
    }

    private fun JsonObject.names(field: String, value: String) {
        val names = value.lines().map { it.trim() }.filter { it.isNotEmpty() }
        require(names.all { it.length <= 255 }) { "Nama anggota keluarga maksimal 255 karakter." }
        if (names.isNotEmpty()) add(field, JsonArray().apply { names.forEach { add(it) } })
    }

    private fun JsonObject.namesText(field: String): String = get(field)?.takeUnless { it.isJsonNull }?.let {
        if (it.isJsonArray) it.asJsonArray.map { name -> name.asString }.joinToString("\n") else it.asString
    }.orEmpty()

    private fun JsonObject.optional(field: String, value: String, max: Int) {
        require(value.trim().length <= max) { "$field maksimal $max karakter." }
        if (value.isNotBlank()) addProperty(field, value.trim())
    }

    private fun JsonObject.text(field: String): String = get(field)?.takeUnless { it.isJsonNull }?.asString.orEmpty()

    fun apiDate(value: String): String {
        for ((pattern, locale) in listOf("yyyy-MM-dd" to Locale.US, "dd MMMM yyyy" to Locale.US,
            "dd MMMM yyyy" to Locale("id", "ID"))) {
            val format = SimpleDateFormat(pattern, locale).apply { isLenient = false }
            val position = java.text.ParsePosition(0)
            val date = format.parse(value, position)
            if (date != null && position.index == value.length) return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
        }
        throw IllegalArgumentException("Tanggal tidak valid. Pilih tanggal melalui kalender.")
    }

    private fun displayDate(value: String): String = if (value.isBlank()) "" else try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(value)
        date?.let { SimpleDateFormat("dd MMMM yyyy", Locale.US).format(it) }.orEmpty()
    } catch (_: Exception) { "" }
}
