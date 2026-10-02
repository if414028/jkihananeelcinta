package com.jki.myhananeelcinta.auth

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.jki.myhananeelcinta.model.Gender
import com.jki.myhananeelcinta.model.Role
import com.jki.myhananeelcinta.model.User
import org.junit.Assert.*
import org.junit.Test

class CmsAuthBridgeTest {
    private fun profile() = JsonParser.parseString("""{
        "success":true,"data":{"account":{"uid":"firebase-uid","email":"auth@example.com"},
        "profile":{"id":42,"full_name":"Maria CMS","nickname":"Maria","gender":"female",
        "email":"profile@example.com","member_number":"HC-2026-00042","is_active":true,
        "date_of_birth":"1995-06-20","address":{"street":"Jl. Gereja","city":"Jakarta","province":null},
        "baptism_status":"baptized","baptism_date":"2010-01-02","marital_status":"widowed"}}}
    """).asJsonObject

    private class Harness(val responses: List<Result<JsonObject>>) {
        val paths = mutableListOf<String>()
        val refreshes = mutableListOf<Boolean>()
        var result: Result<User>? = null
        val bridge = CmsAuthBridge({ refreshed, done ->
            refreshes.add(refreshed)
            done(Result.success(if (refreshed) "new-token" else "cached-token"))
        }, { path, _, _, done ->
            paths.add(path)
            done(responses[paths.lastIndex])
        })
        fun register() = bridge.register(JsonObject()) { result = it }
        fun login() = bridge.openSession { result = it }
    }

    @Test fun loginRefreshesOnlyOnceAfter401() {
        val harness = Harness(listOf(Result.failure(CmsAuthException(401)), Result.success(profile())))
        harness.login()
        assertEquals(listOf("/auth/session", "/auth/session"), harness.paths)
        assertEquals(listOf(false, true), harness.refreshes)
        assertEquals("firebase-uid", harness.result!!.getOrThrow().id)
    }

    @Test fun repeated401RequiresLoginWithoutAnotherRetry() {
        val harness = Harness(List(2) { Result.failure<JsonObject>(CmsAuthException(401)) })
        harness.login()
        assertEquals(2, harness.paths.size)
        assertEquals(401, (harness.result!!.exceptionOrNull() as CmsAuthException).status)
    }

    @Test fun unlinkedOrInactiveLoginDoesNotRegister() {
        val harness = Harness(listOf(Result.failure(CmsAuthException(403))))
        harness.login()
        assertEquals(listOf("/auth/session"), harness.paths)
        assertEquals(403, (harness.result!!.exceptionOrNull() as CmsAuthException).status)
    }

    @Test fun timeoutChecksMeBeforeReportingFailure() {
        val harness = Harness(listOf(Result.failure(CmsAuthException()), Result.success(profile())))
        harness.register()
        assertEquals(listOf("/auth/register", "/me"), harness.paths)
        assertTrue(harness.result!!.isSuccess)
    }

    @Test fun conflictCanRecoverAnAlreadyCommittedRegistration() {
        val harness = Harness(listOf(Result.failure(CmsAuthException(409)), Result.success(profile())))
        harness.register()
        assertTrue(harness.result!!.isSuccess)
        assertEquals(listOf("/auth/register", "/me"), harness.paths)
    }

    @Test fun unresolvedConflictRequiresAdmin() {
        val harness = Harness(listOf(Result.failure(CmsAuthException(409)), Result.failure(CmsAuthException(403))))
        harness.register()
        assertEquals(409, (harness.result!!.exceptionOrNull() as CmsAuthException).status)
    }

    @Test fun invalidTokenDuringConflictRecoveryStillRequiresLogin() {
        val harness = Harness(listOf(Result.failure(CmsAuthException(409)),
            Result.failure(CmsAuthException(401)), Result.failure(CmsAuthException(401))))
        harness.register()
        assertEquals(401, (harness.result!!.exceptionOrNull() as CmsAuthException).status)
        assertEquals(listOf(false, false, true), harness.refreshes)
    }

    @Test fun validationAndRateLimitErrorsDoNotResubmitRegistration() {
        for (status in listOf(422, 429, 403)) {
            val harness = Harness(listOf(Result.failure(CmsAuthException(status))))
            harness.register()
            assertEquals(listOf("/auth/register"), harness.paths)
            assertEquals(status, (harness.result!!.exceptionOrNull() as CmsAuthException).status)
        }
    }

    @Test fun cmsProfileOverridesLegacyDataWithoutGrantingAdminPermissions() {
        val user = CmsProfileMapper.session(profile())
        assertEquals("Maria CMS", user.fullName)
        assertEquals("profile@example.com", user.email)
        assertEquals("HC-2026-00042", user.nij)
        assertEquals(Gender.FEMALE.gender, user.gender)
        assertEquals("Jl. Gereja, Jakarta", user.address)
        assertEquals("20 June 1995", user.dateOfBirth)
        assertEquals("widowed", user.maritalStatus)
        assertEquals(Role.JEMAAT.role, user.role)
        assertEquals("", user.password)
    }

    @Test fun inactiveProfileIsRejectedEvenIfResponseIsSuccessful() {
        val json = profile()
        json.getAsJsonObject("data").getAsJsonObject("profile").addProperty("is_active", false)
        val error = runCatching { CmsProfileMapper.session(json) }.exceptionOrNull() as CmsAuthException
        assertEquals(403, error.status)
    }

    @Test fun registrationConvertsDatesAndOnlySendsAllowedFields() {
        val user = User().apply {
            fullName = " Maria "
            gender = Gender.FEMALE.gender
            username = "Maria"
            email = "private@example.com"
            password = "never-send-this"
            id = "never-send-uid"
            role = Role.SUPERUSER.role
            dateOfBirth = "20 Juni 1995"
            waterBaptism = true
            waterBaptisteryDate = "02 January 2010"
            married = true
        }
        val body = CmsProfileMapper.registration(user)
        assertEquals("Maria", body["full_name"].asString)
        assertEquals("female", body["gender"].asString)
        assertEquals("1995-06-20", body["date_of_birth"].asString)
        assertEquals("2010-01-02", body["baptism_date"].asString)
        assertEquals("married", body["marital_status"].asString)
        for (field in listOf("email", "uid", "id", "password", "role", "nij", "fcmToken", "photoImageUrl")) assertFalse(body.has(field))
    }

    @Test fun optionalFieldsCanBeOmittedAndNonBaptizedDateIsNotSent() {
        val user = User().apply { fullName = "Maria"; gender = "female"; waterBaptisteryDate = "old-value" }
        val body = CmsProfileMapper.registration(user)
        assertFalse(body.has("date_of_birth"))
        assertFalse(body.has("phone_number"))
        assertFalse(body.has("baptism_date"))
        assertEquals("not_baptized", body["baptism_status"].asString)
    }

    @Test fun baptismRequiresAValidDateBeforeCreatingFirebaseAccount() {
        val user = User().apply { fullName = "Maria"; gender = "female"; waterBaptism = true }
        assertTrue(runCatching { CmsProfileMapper.registration(user) }.isFailure)
        user.waterBaptisteryDate = "2026-02-31"
        assertTrue(runCatching { CmsProfileMapper.registration(user) }.isFailure)
    }
}
