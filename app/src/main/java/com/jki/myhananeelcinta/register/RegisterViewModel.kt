package com.jki.myhananeelcinta.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.gson.JsonObject
import com.jki.myhananeelcinta.auth.CmsAuthRepository
import com.jki.myhananeelcinta.auth.CmsProfileMapper
import com.jki.myhananeelcinta.model.User
import com.jki.myhananeelcinta.util.SingleLiveEvent
import com.jki.myhananeelcinta.util.UserConfiguration
import java.io.File


class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var password = ""
    val isSubmitting = MutableLiveData(false)
    val form = RegistrationForm()

    fun submitForm() {
        if ((0..5).any { form.errors(it).isNotEmpty() }) return
        user = form.profile()
        password = form.password
        signUpUser()
    }

    var user: User = User()
    var capturedImageFile: File? = null

    var isSectionValid: SingleLiveEvent<Boolean> = SingleLiveEvent()
    var isSuccessCreateNewUser: SingleLiveEvent<String> = SingleLiveEvent()
    var isFailCreateNewUser: SingleLiveEvent<String> = SingleLiveEvent()
    var isUserAlreadyRegistered: SingleLiveEvent<String> = SingleLiveEvent()
    var isEnableToSubmit: SingleLiveEvent<Boolean> = SingleLiveEvent()

    fun setUserCredential(username: String, email: String, password: String, rePassword: String) {
        user.username = username
        user.email = email.trim()
        this.password = if (password == rePassword) password else ""
        user.password = ""
    }

    fun setUserInformation(
        fullName: String,
        gender: String,
        placeOfBirth: String,
        dateOfBirth: String,
        phoneNumber: String
    ) {
        if (fullName.isNotBlank() && gender.isNotBlank()) {
            user.fullName = fullName
            user.gender = gender
            user.placeOfBirth = placeOfBirth
            user.dateOfBirth = dateOfBirth
            user.phoneNumber = phoneNumber
        }
    }

    fun setUserAddress(address: String) {
        user.address = address.trim()
    }

    fun setUserEducation(bloodType: String, lastEducation: String, job: String) {
        user.bloodType = bloodType
        user.lastEducation = lastEducation
        user.job = job
    }

    fun setUserBaptismInformation(
        waterBaptism: String,
        waterBaptisteryChurch: String,
        waterBaptisteryDate: String,
        holySpiritBaptism: String,
        churchOrigin: String,
        reasonToMovingChurch: String
    ) {
        user.waterBaptism = waterBaptism == "Sudah"
        user.waterBaptisteryChurch = waterBaptisteryChurch
        user.waterBaptisteryDate = waterBaptisteryDate
        user.holySpiritBaptism = holySpiritBaptism == "Sudah"
        user.churchOrigin = churchOrigin
        user.reasonToMovingChurch = reasonToMovingChurch
    }

    fun setUserMartialStatus(
        married: String,
        statusInFamily: String
    ) {
        user.married = married == "Sudah Menikah" || married == "married"
        user.maritalStatus = married
        user.statusInFamily = statusInFamily
    }

    fun setHeadOfFamilyId(headOfFamilyId: String) {
        user.headOfFamilyId = headOfFamilyId
    }

    fun setUserSelfieImagePath(imagePath: String) {
        if (!imagePath.isBlank()) {
            user.photoImageUrl = imagePath
        }
    }

    fun setFamilyMemberData(
        wifeName: String,
        husbandName: String,
        childrenName: String,
        siblingsName: String
    ) {
        if (wifeName.isNotEmpty() && wifeName.isNotEmpty()) {
            user.wifeName = wifeName
        }

        if (husbandName.isNotEmpty() && husbandName.isNotEmpty()) {
            user.husbandName = husbandName
        }

        if (childrenName.isNotEmpty() && childrenName.isNotEmpty()) {
            user.childrenName = childrenName
        }

        if (siblingsName.isNotEmpty() && siblingsName.isNotEmpty()) {
            user.siblingsName = siblingsName
        }
    }

    fun setIsSectionValid(isValid: Boolean) {
        isSectionValid.postValue(isValid)
    }

    fun setIsEnableToSubmit(isValid: Boolean) {
        isEnableToSubmit.postValue(isValid)
    }

    fun signUpUser() {
        if (isSubmitting.value == true) return
        val payload = try {
            CmsProfileMapper.registration(user)
        } catch (error: IllegalArgumentException) {
            isFailCreateNewUser.value = error.message
            return
        }
        if (form.holySpiritStatus.isBlank()) payload.remove("holy_spirit_baptism")
        if (capturedImageFile != null && capturedImageFile?.isFile != true) {
            isFailCreateNewUser.value = "File foto tidak tersedia. Silakan ambil ulang foto."
            return
        }
        if (password.length < 6) {
            isFailCreateNewUser.value = "Password minimal 6 karakter."
            return
        }
        isSubmitting.value = true
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null && currentUser.email.equals(user.email, ignoreCase = true)
            && UserConfiguration.getInstance().isPendingRegistration(currentUser.uid)) {
            registerCms(payload)
            return
        }
        firebaseAuth.createUserWithEmailAndPassword(user.email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = task.result?.user?.uid
                if (uid != null) {
                    UserConfiguration.getInstance().setPendingRegistration(uid)
                    registerCms(payload)
                } else fail("Akun Firebase tidak tersedia. Silakan coba lagi.")
            } else if (task.exception is FirebaseAuthUserCollisionException) {
                // Only an account created by an unfinished signup may resume CMS registration.
                firebaseAuth.signInWithEmailAndPassword(user.email, password).addOnCompleteListener { login ->
                    val uid = if (login.isSuccessful) login.result?.user?.uid else null
                    if (uid != null && UserConfiguration.getInstance().isPendingRegistration(uid)) registerCms(payload)
                    else {
                        isSubmitting.value = false
                        isUserAlreadyRegistered.value = "Email sudah terdaftar. Silakan login."
                    }
                }
            } else fail(task.exception?.localizedMessage ?: "Registrasi Firebase gagal. Silakan coba lagi.")
        }
    }

    private fun registerCms(payload: JsonObject) {
        val repository = CmsAuthRepository.getInstance(getApplication())
        repository.register(payload, capturedImageFile) { result ->
            result.fold({ profile ->
                repository.saveSession(profile)
                UserConfiguration.getInstance().clearPendingRegistration(profile.id)
                password = ""
                form.password = ""
                form.confirmation = ""
                user.password = ""
                capturedImageFile?.delete()
                capturedImageFile = null
                form.photoPath = ""
                isSubmitting.value = false
                isSuccessCreateNewUser.value = "Success"
            }, { error ->
                repository.handleFailure(error)
                fail((error.message ?: "Registrasi CMS gagal.") +
                    " Akun Firebase tetap tersimpan; perbaiki data atau coba registrasi lagi dengan email yang sama.")
            })
        }
    }

    private fun fail(message: String) {
        isSubmitting.value = false
        isFailCreateNewUser.value = message
    }
}
