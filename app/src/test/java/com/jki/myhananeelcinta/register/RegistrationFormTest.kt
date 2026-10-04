package com.jki.myhananeelcinta.register

import com.jki.myhananeelcinta.auth.CmsProfileMapper
import com.jki.myhananeelcinta.model.User
import org.junit.Assert.*
import org.junit.Test

class RegistrationFormTest {
    @Test fun accountReportsSpecificErrorsWithoutAdvancing() {
        val form = RegistrationForm()
        assertEquals(setOf("email", "password"), form.errors(0))
        assertEquals(0, form.step)
        form.email = "maria@example.com"
        form.password = "correct-password"
        form.confirmation = "different-password"
        assertEquals(setOf("confirmation"), form.errors(0))
    }

    @Test fun personalInformationRequiresAllRequestedFields() {
        val form = RegistrationForm(fullName = "Maria")
        assertEquals(setOf("gender", "birthplace", "birthdate", "phone"), form.errors(1))
        form.gender = "female"
        form.phone = "08123456789"
        form.birthplace = "Jakarta"
        form.birthdate = "1995-06-20"
        assertTrue(form.errors(1).isEmpty())
        assertEquals(setOf("address", "occupation", "blood", "education"), form.errors(2))
    }

    @Test fun phoneNormalizationIsValidatedAfterAddingCountryCode() {
        val form = RegistrationForm(fullName = "Maria", gender = "female", phone = "08123456789", birthplace = "Jakarta", birthdate = "1995-06-20")
        assertEquals("+628123456789", form.formattedPhone())
        assertTrue(form.errors(1).isEmpty())
        form.phone = "123"
        assertEquals(setOf("phone"), form.errors(1))
    }

    @Test fun baptismDateIsRequiredOnlyForBaptizedMembers() {
        val form = RegistrationForm(baptismStatus = "baptized")
        assertEquals(setOf("baptism_date"), form.errors(3, "2026-10-02"))
        form.baptismDate = "2026-10-03"
        assertEquals(setOf("baptism_date"), form.errors(3, "2026-10-02"))
        form.baptismDate = "2026-10-02"
        assertTrue(form.errors(3, "2026-10-02").isEmpty())
        form.baptismStatus = "unknown"
        form.baptismDate = ""
        assertTrue(form.errors(3).isEmpty())
    }

    @Test fun birthdateMustBeValidAndBeforeToday() {
        val form = RegistrationForm(fullName = "Maria", gender = "female", phone = "08123456789", birthplace = "Jakarta", birthdate = "2026-10-02")
        assertEquals(setOf("birthdate"), form.errors(1, "2026-10-02"))
        form.birthdate = "2026-02-31"
        assertEquals(setOf("birthdate"), form.errors(1, "2026-10-02"))
        form.birthdate = "1995-06-20"
        assertTrue(form.errors(1, "2026-10-02").isEmpty())
    }

    @Test fun unknownBaptismStatusDoesNotDeclareUserNotBaptized() {
        val user = User().apply { fullName = "Maria"; gender = "female"; baptismStatus = "unknown" }
        val payload = CmsProfileMapper.registration(user)
        assertEquals("unknown", payload["baptism_status"].asString)
        assertFalse(payload.has("marital_status"))
        assertFalse(payload.has("baptism_date"))
    }
    @Test fun requiredFieldsRejectWhitespaceAndUnselectedChoices() {
        val form = RegistrationForm(fullName = "Maria", gender = "female", phone = "   ",
            birthplace = "   ", address = "   ", occupation = "   ",
            bloodType = "Belum diisi", education = "Belum diisi", maritalStatus = "")
        assertEquals(setOf("phone", "birthplace", "birthdate"), form.errors(1))
        assertEquals(setOf("address", "occupation", "blood", "education"), form.errors(2))
        assertEquals(setOf("marital"), form.errors(4))
        form.address = "Jl. Cinta"
        form.occupation = "Guru"
        form.bloodType = "O"
        form.education = "S1"
        form.maritalStatus = "single"
        assertTrue(form.errors(2).isEmpty())
        assertTrue(form.errors(4).isEmpty())
    }

    @Test fun familyChoicePreservesDraftButOmitsHiddenNamesFromValidationAndPayload() {
        val form = RegistrationForm(maritalStatus = "married", wifeName = "Maria", husbandName = "Andi",
            childrenNames = "Anak satu", siblingsNames = "Saudara satu")
        for ((status, hidden) in listOf("Kepala Keluarga" to "husband", "Istri" to "wife", "Anak" to "children")) {
            form.familyStatus = status
            val profile = form.profile()
            assertEquals(if (hidden == "husband") "" else "Andi", profile.husbandName)
            assertEquals(if (hidden == "wife") "" else "Maria", profile.wifeName)
            assertEquals(if (hidden == "children") "" else "Anak satu", profile.childrenName)
        }
        form.familyStatus = "Anak"
        form.childrenNames = "x".repeat(256)
        assertTrue(form.errors(4).isEmpty())
        form.familyStatus = "Istri"
        assertEquals(setOf("children"), form.errors(4))
        assertEquals("Andi", form.husbandName)
        assertEquals("Maria", form.wifeName)
        assertEquals("x".repeat(256), form.childrenNames)
    }

}
