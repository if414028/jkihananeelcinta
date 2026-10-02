package com.jki.myhananeelcinta.register

import com.jki.myhananeelcinta.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Draft stays in ViewModel memory. Passwords are never written to a saved-state bundle. */
data class RegistrationForm(
    var step: Int = 0,
    var email: String = "",
    var password: String = "",
    var confirmation: String = "",
    var fullName: String = "",
    var gender: String = "",
    var nickname: String = "",
    var phone: String = "",
    var birthplace: String = "",
    var birthdate: String = "",
    var address: String = "",
    var occupation: String = "",
    var maritalStatus: String = "",
    var baptismStatus: String = "unknown",
    var baptismDate: String = "",
    var bloodType: String = "",
    var education: String = "",
    var baptismChurch: String = "",
    var holySpiritStatus: String = "",
    var churchOrigin: String = "",
    var movingReason: String = "",
    var familyStatus: String = "",
    var wifeName: String = "",
    var husbandName: String = "",
    var childrenNames: String = "",
    var siblingsNames: String = "",
    var photoPath: String = ""
) {
    fun errors(step: Int, today: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())): Set<String> = linkedSetOf<String>().apply {
        when (step) {
            0 -> {
                if (!email.trim().matches(Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))) add("email")
                if (password.length < 6 || password.isBlank()) add("password")
                if (confirmation != password) add("confirmation")
            }
            1 -> {
                if (fullName.trim().length !in 1..255) add("name")
                if (gender !in listOf("male", "female")) add("gender")
                if (nickname.trim().length > 100) add("nickname")
                if (birthplace.trim().length > 100) add("birthplace")
                if (birthdate.isNotBlank() && (!validDate(birthdate) || birthdate >= today)) add("birthdate")
                val number = formattedPhone()
                if (number.isNotEmpty() && (number.length !in 7..30 || !number.matches(Regex("[0-9+() .-]+")))) add("phone")
            }
            2 -> {
                if (address.trim().length > 2000) add("address")
                if (occupation.trim().length > 150) add("occupation")
                if (bloodType.trim().length > 3) add("blood")
                if (education.trim().length > 100) add("education")
            }
            3 -> {
                if (baptismChurch.trim().length > 255) add("baptism_church")
                if (churchOrigin.trim().length > 255) add("church_origin")
                if (movingReason.trim().length > 2000) add("moving_reason")
                if (baptismStatus == "baptized" && (!validDate(baptismDate) || baptismDate > today)) add("baptism_date")
            }
            4 -> {
                if (familyStatus.trim().length > 100) add("family_status")
                if (wifeName.trim().length > 255) add("wife")
                if (husbandName.trim().length > 255) add("husband")
                if (childrenNames.lines().any { it.trim().length > 255 }) add("children")
                if (siblingsNames.lines().any { it.trim().length > 255 }) add("siblings")
            }
        }
    }

    fun profile(): User = User().apply {
        email = this@RegistrationForm.email.trim()
        username = nickname.trim()
        fullName = this@RegistrationForm.fullName.trim()
        gender = this@RegistrationForm.gender
        placeOfBirth = birthplace.trim()
        dateOfBirth = birthdate
        phoneNumber = formattedPhone()
        address = this@RegistrationForm.address.trim()
        bloodType = this@RegistrationForm.bloodType
        lastEducation = education
        job = occupation.trim()
        maritalStatus = this@RegistrationForm.maritalStatus
        married = maritalStatus == "married"
        baptismStatus = this@RegistrationForm.baptismStatus
        waterBaptism = baptismStatus == "baptized"
        waterBaptisteryDate = if (waterBaptism) baptismDate else ""
        waterBaptisteryChurch = baptismChurch.trim()
        holySpiritBaptism = holySpiritStatus == "Sudah"
        churchOrigin = this@RegistrationForm.churchOrigin.trim()
        reasonToMovingChurch = movingReason.trim()
        statusInFamily = familyStatus
        wifeName = this@RegistrationForm.wifeName.trim()
        husbandName = this@RegistrationForm.husbandName.trim()
        childrenName = childrenNames.trim()
        siblingsName = siblingsNames.trim()
    }

    fun formattedPhone(): String = phone.trim().let { if (it.startsWith("0")) "+62${it.substring(1)}" else it }

    private fun validDate(value: String): Boolean {
        if (!value.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return false
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        return runCatching { format.parse(value) != null }.getOrDefault(false)
    }
}
