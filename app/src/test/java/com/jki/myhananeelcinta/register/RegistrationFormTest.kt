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

    @Test fun personalInformationRequiresAnExplicitGenderButOptionalFieldsCanBeSkipped() {
        val form = RegistrationForm(fullName = "Maria")
        assertEquals(setOf("gender"), form.errors(1))
        form.gender = "female"
        assertTrue(form.errors(1).isEmpty())
        assertTrue(form.errors(2).isEmpty())
    }

    @Test fun phoneNormalizationIsValidatedAfterAddingCountryCode() {
        val form = RegistrationForm(fullName = "Maria", gender = "female", phone = "08123456789")
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
        val form = RegistrationForm(fullName = "Maria", gender = "female", birthdate = "2026-10-02")
        assertEquals(setOf("birthdate"), form.errors(1, "2026-10-02"))
        form.birthdate = "2026-02-31"
        assertEquals(setOf("birthdate"), form.errors(1, "2026-10-02"))
        form.birthdate = "1995-06-20"
        assertTrue(form.errors(1, "2026-10-02").isEmpty())
    }

    @Test fun skippingOptionalStatusDoesNotDeclareUserSingleOrNotBaptized() {
        val user = User().apply { fullName = "Maria"; gender = "female"; baptismStatus = "unknown" }
        val payload = CmsProfileMapper.registration(user)
        assertEquals("unknown", payload["baptism_status"].asString)
        assertFalse(payload.has("marital_status"))
        assertFalse(payload.has("baptism_date"))
    }
}
