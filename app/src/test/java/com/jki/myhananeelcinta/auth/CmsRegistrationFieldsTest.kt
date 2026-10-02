package com.jki.myhananeelcinta.auth

import com.google.gson.JsonArray
import com.jki.myhananeelcinta.register.RegistrationForm
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class CmsRegistrationFieldsTest {
    private fun completeForm() = RegistrationForm(
        email = "maria@example.com", password = "secret-password", confirmation = "secret-password",
        fullName = "Maria", gender = "female", nickname = "Mia", phone = "08123456789",
        birthplace = "Jakarta", birthdate = "1990-01-02", address = "Jl. Cinta 1",
        bloodType = "AB", education = "S1", occupation = "Pegawai Swasta",
        maritalStatus = "married", baptismStatus = "baptized", baptismDate = "2010-01-02",
        baptismChurch = "Gereja baptis", holySpiritStatus = "Sudah", churchOrigin = "Gereja asal",
        movingReason = "Pindah kota", familyStatus = "Istri", wifeName = "Maria", husbandName = "Andi",
        childrenNames = "Anak satu\nAnak dua", siblingsNames = "Saudara satu\nSaudara dua"
    )

    @Test fun everyLegacyProfileFieldReachesTheCmsPayload() {
        val form = completeForm()
        assertTrue((0..5).all { form.errors(it).isEmpty() })
        val body = CmsProfileMapper.registration(form.profile())
        val expected = mapOf("full_name" to "Maria", "nickname" to "Mia", "gender" to "female",
            "phone_number" to "+628123456789", "place_of_birth" to "Jakarta", "date_of_birth" to "1990-01-02",
            "address" to "Jl. Cinta 1", "blood_type" to "AB", "last_education" to "S1",
            "occupation" to "Pegawai Swasta", "marital_status" to "married", "baptism_status" to "baptized",
            "baptism_date" to "2010-01-02", "baptism_church" to "Gereja baptis", "church_origin" to "Gereja asal",
            "reason_to_move_church" to "Pindah kota", "family_status" to "Istri", "wife_name" to "Maria", "husband_name" to "Andi")
        expected.forEach { (key, value) -> assertEquals(key, value, body[key].asString) }
        assertTrue(body["holy_spirit_baptism"].asBoolean)
        assertEquals(listOf("Anak satu", "Anak dua"), body["children_names"].asJsonArray.map { it.asString })
        assertEquals(listOf("Saudara satu", "Saudara dua"), body["siblings_names"].asJsonArray.map { it.asString })
        for (key in listOf("email", "password", "uid", "role", "nij", "photoPath")) assertFalse(body.has(key))
    }

    @Test fun multipartPreservesUtf8MultipleNamesBooleanAndPhotoBytes() {
        val fields = CmsProfileMapper.registration(completeForm().profile())
        fields.add("children_names", JsonArray().apply { add("Anak José"); add("Anak dua") })
        val photo = File.createTempFile("registration-test", ".jpg")
        try {
            val jpeg = byteArrayOf(0xff.toByte(), 0xd8.toByte(), 1, 2, 0xff.toByte(), 0xd9.toByte())
            photo.writeBytes(jpeg)
            val body = CmsRegistrationMultipart.encode(fields, photo, "test-boundary")
            val text = body.toString(Charsets.UTF_8)
            assertTrue(text.contains("name=\"holy_spirit_baptism\"\r\n\r\n1\r\n"))
            assertTrue(text.contains("name=\"children_names[]\"\r\n\r\nAnak José\r\n"))
            assertTrue(text.contains("name=\"children_names[]\"\r\n\r\nAnak dua\r\n"))
            assertTrue(text.contains("name=\"profile_photo\"; filename=\"profile.jpg\"\r\nContent-Type: image/jpeg"))
            val start = body.indices.first { i -> i + jpeg.size <= body.size && body.copyOfRange(i, i + jpeg.size).contentEquals(jpeg) }
            assertArrayEquals(jpeg, body.copyOfRange(start, start + jpeg.size))
            assertTrue(text.endsWith("\r\n--test-boundary--\r\n"))
        } finally { photo.delete() }
    }

    @Test fun oversizedLegacyFieldsAreReportedInTheirCategory() {
        val form = completeForm().apply { movingReason = "x".repeat(2001); husbandName = "x".repeat(256) }
        assertEquals(setOf("moving_reason"), form.errors(3))
        assertEquals(setOf("husband"), form.errors(4))
    }
}
