package com.jki.hananeelcinta.register

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jki.hananeelcinta.model.Role
import com.jki.hananeelcinta.model.User
import com.jki.hananeelcinta.util.PictureUploader
import com.jki.hananeelcinta.util.SingleLiveEvent
import com.jki.hananeelcinta.util.UserConfiguration
import java.io.File


class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private var database: DatabaseReference = Firebase.database.getReference("users")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val profilePictureUploader = PictureUploader()

    var user: User = User()
    var capturedImageFile: File? = null

    var isSectionValid: SingleLiveEvent<Boolean> = SingleLiveEvent()
    var isSuccessCreateNewUser: SingleLiveEvent<String> = SingleLiveEvent()
    var isFailCreateNewUser: SingleLiveEvent<String> = SingleLiveEvent()
    var isUserAlreadyRegistered: SingleLiveEvent<String> = SingleLiveEvent()
    var isEnableToSubmit: SingleLiveEvent<Boolean> = SingleLiveEvent()

    fun setUserCredential(username: String, email: String, password: String, rePassword: String) {
        user.username = username
        user.email = email
        user.password = password
    }

    fun setUserInformation(
        fullName: String,
        gender: String,
        placeOfBirth: String,
        dateOfBirth: String,
        phoneNumber: String
    ) {
        if (fullName.isNotBlank() && gender.isNotBlank() && placeOfBirth.isNotBlank() && dateOfBirth.isNotBlank() && phoneNumber.isNotBlank()) {
            user.fullName = fullName
            user.gender = gender
            user.placeOfBirth = placeOfBirth
            user.dateOfBirth = dateOfBirth
            user.phoneNumber = phoneNumber
        }
    }

    fun setUserAddress(address: String) {
        if (address.isNotBlank()) {
            user.address = address
        }
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
        user.married = married == "Sudah Menikah"
        user.statusInFamily = statusInFamily
    }

    fun setUserSelfieImagePath(imagePath: String) {
        if (!imagePath.isBlank()) {
            user.photoImageUrl = imagePath
        }
    }

    fun setIsSectionValid(isValid: Boolean) {
        isSectionValid.postValue(isValid)
    }

    fun setIsEnableToSubmit(isValid: Boolean) {
        isEnableToSubmit.postValue(isValid)
    }

    fun signUpUser() {
        firebaseAuth.fetchSignInMethodsForEmail(user.email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val signInMethods = task.result?.signInMethods
                if (!signInMethods.isNullOrEmpty()) {
                    //User already registered
                    isUserAlreadyRegistered.postValue("User already registered")
                } else {
                    // User is not registered, proceed with account creation
                    createUserWithEmailAndPassword()
                }
            } else {

            }
        }
    }

    private fun generateNIJ() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userCount = snapshot.childrenCount
                user.nij = "HC-%05d".format(userCount)

                writeUserData()
            }

            override fun onCancelled(error: DatabaseError) {
                isFailCreateNewUser.postValue("Gagal generate NIJ")
            }

        })
    }

    private fun createUserWithEmailAndPassword() {
        firebaseAuth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    user.id = firebaseAuth.currentUser!!.uid
                    generateNIJ()
                } else {
                    isFailCreateNewUser.postValue(it.exception?.localizedMessage)
                }
            }
    }

    private fun writeUserData() {
        //avoid write password in database, password only saved in authentication
        user.password = ""
        user.role = Role.JEMAAT.role

        database.child(user.id)
            .setValue(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    UserConfiguration.getInstance().setUserId(user.id)
                    UserConfiguration.getInstance().setUserData(user)
                    uploadProfilePicture()
                } else {
                    val exception = task.exception
                    if (exception != null) {
                        isFailCreateNewUser.postValue(exception.localizedMessage)
                    }
                }
            }
    }

    private fun uploadProfilePicture() {
        if (capturedImageFile != null) {
            profilePictureUploader.uploadProfilePicture(capturedImageFile!!.toUri()) { imageUrl, error ->
                if (error != null) {
                    isFailCreateNewUser.postValue(error)
                } else {
                    isSuccessCreateNewUser.postValue("Success")
                }
            }
        } else {
            isFailCreateNewUser.postValue("Fail to upload profile picture")
        }
    }
}