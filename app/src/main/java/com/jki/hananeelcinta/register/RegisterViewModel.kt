package com.jki.hananeelcinta.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jki.hananeelcinta.model.User
import com.jki.hananeelcinta.util.SingleLiveEvent
import com.jki.hananeelcinta.util.UserConfiguration


class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private var database: DatabaseReference = Firebase.database.getReference("users")
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var user: User = User()

    var isSectionValid: SingleLiveEvent<Boolean> = SingleLiveEvent()
    var isSuccessCreateNewUser: SingleLiveEvent<Void> = SingleLiveEvent()
    var isFailCreateNewUser: SingleLiveEvent<String> = SingleLiveEvent()
    var isUserAlreadyRegistered: SingleLiveEvent<Void> = SingleLiveEvent()

    fun setUserCredential(username: String, email: String, password: String, rePassword: String) {
        user.username = username
        user.email = email
        user.password = password
    }

    fun setUserInformation(
        fullName: String,
        idNumber: String,
        gender: String,
        placeOfBirth: String,
        dateOfBirth: String,
        phoneNumber: String
    ) {
        if (!fullName.isBlank() && !idNumber.isBlank() && !gender.isBlank() && !placeOfBirth.isBlank() && !dateOfBirth.isBlank() && !phoneNumber.isBlank()) {
            user.fullName = fullName
            user.idNumber = idNumber.toLong()
            user.gender = gender
            user.placeOfBirth = placeOfBirth
            user.dateOfBirth = dateOfBirth
            user.phoneNumber = phoneNumber
        }
    }

    fun setUserAddress(address: String) {
        if (!address.isBlank()) {
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
        fatherFullName: String,
        motherFullName: String,
        statusInFamily: String
    ) {
        user.married = married == "Sudah Menikah"
        user.fatherFullName = fatherFullName
        user.motherFullName = motherFullName
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

    fun signUpUser() {
        firebaseAuth.fetchSignInMethodsForEmail(user.email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val signInMethods = task.result?.signInMethods
                if (!signInMethods.isNullOrEmpty()) {
                    //User already registered
                    isUserAlreadyRegistered.callFromBackgroundThread()
                } else {
                    // User is not registered, proceed with account creation
                    createUserWithEmailAndPassword()
                }
            } else {

            }
        }


    }

    private fun createUserWithEmailAndPassword() {
        firebaseAuth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    user.id = firebaseAuth.currentUser!!.uid
                    writeUserData()
                } else {
                    isFailCreateNewUser.callFromBackgroundThread()
                }
            }
    }

    private fun writeUserData() {
        //avoid write password in database, password only saved in authentication
        user.password = ""

        database.child(user.id)
            .setValue(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    UserConfiguration.getInstance().setUserId(user.id)
                    UserConfiguration.getInstance().setUserData(user)
                    isSuccessCreateNewUser.callFromBackgroundThread()
                } else {
                    val exception = task.exception
                    if (exception != null) {
                        isFailCreateNewUser.postValue(exception.localizedMessage)
                    }
                }
            }
    }
}