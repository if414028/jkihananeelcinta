package com.jki.hananeelcinta.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityProfileBinding
import com.jki.hananeelcinta.login.LoginActivity
import com.jki.hananeelcinta.pastoral.congregation.CongregationDetailActivity
import com.jki.hananeelcinta.util.UserConfiguration

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth

    val userData = UserConfiguration.getInstance().getUserData()
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)
        supportActionBar?.hide()

        setupLayout()
    }

    private fun setupLayout() {
        if (userData != null) {
            binding.username = userData.fullName
            binding.nij = "NIJ: ${userData.nij}"
            getProfileImage()
        }
        binding.btnSignOut.setOnClickListener { signOut() }
        binding.btnProfile.setOnClickListener { detailProfile() }
    }

    private fun detailProfile() {
        val intent =
            Intent(applicationContext, CongregationDetailActivity::class.java)
        intent.putExtra(CongregationDetailActivity.USER_ID_ARG, userData?.id)
        startActivity(intent)
    }

    private fun signOut() {
        firebaseAuth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun getProfileImage() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { currentUser ->
            val profilePicturesRef = storageRef.child("${currentUser.uid}/profile-pictures")
            profilePicturesRef.downloadUrl.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.getResult()
                    val imageUrl = downloadUri.toString()
                    val requestOptions = RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both the original and resized image
                        .centerCrop() // Center-crop the image to fit the ImageView

                    Glide.with(applicationContext)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_no_profile_image)
                        .apply(requestOptions)
                        .into(binding.ivProfile)
                }
            }
        }
    }
}