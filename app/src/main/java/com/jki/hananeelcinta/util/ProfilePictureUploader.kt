package com.jki.hananeelcinta.util

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProfilePictureUploader {

    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference

    fun uploadProfilePicture(
        imageUri: Uri,
        onComplete: (imageUrl: String?, error: String?) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { currentUser ->
            val profilePicturesRef = storageRef.child("${currentUser.uid}/profile-pictures")
            val uploadTask = profilePicturesRef.putFile(imageUri)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                profilePicturesRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    onComplete(downloadUri.toString(), null)
                } else {
                    onComplete(null, task.exception?.message)
                }
            }
        } ?: run {
            onComplete(null, "User not authenticated.")
        }
    }

}