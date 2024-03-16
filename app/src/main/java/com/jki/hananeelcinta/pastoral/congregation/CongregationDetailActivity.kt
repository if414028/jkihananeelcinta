package com.jki.hananeelcinta.pastoral.congregation

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityCongregationDetailBinding
import com.jki.hananeelcinta.model.User
import com.jki.hananeelcinta.util.UserConfiguration

class CongregationDetailActivity : AppCompatActivity() {

    companion object {
        const val USER_ID_ARG = "userIdArg"
    }

    private val database = FirebaseDatabase.getInstance()
    private val userRef = database.getReference("users")
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference

    private lateinit var binding: ActivityCongregationDetailBinding
    private lateinit var userId: String

    val userData = UserConfiguration.getInstance().getUserData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_congregation_detail)
        supportActionBar?.hide()

        getAdditionalData()
        setupLayout()
        getDetailUserData()
    }

    private fun getAdditionalData() {
        userId = intent.getStringExtra(USER_ID_ARG).toString()
    }

    private fun setupLayout() {
        binding.btnBack.setOnClickListener { onBackPressed() }
        setupBtnEditProfile()
    }

    private fun getDetailUserData() {
        userRef.child(userId).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.getValue(User::class.java) != null) {
                            val userModel = snapshot.getValue(User::class.java)
                            binding.model = snapshot.getValue(User::class.java)
                            loadProfileImage()
                            userModel?.let { setupWhatsappButton(it.phoneNumber) }
                            binding.isLoading = false
                        } else {
                            binding.isError = true
                        }
                    } else {
                        binding.isError = true
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.isError = true
                }

            }
        )
    }

    private fun setupWhatsappButton(phoneNumber: String) {
        if (userData?.id.equals(userId)) {
            binding.btnWhatsapp.visibility = View.VISIBLE
            binding.btnWhatsapp.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data =
                    Uri.parse("https://api.whatsapp.com/send?phone=${phoneNumber}")
                startActivity(intent)
            }
        }
    }

    private fun loadProfileImage() {
        val profilePicturesRef = storageRef.child("${userId}/profile-pictures")
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
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .apply(requestOptions)
                    .into(binding.ivProfile)
            }
        }
    }

    private fun setupBtnEditProfile() {
        if (userData?.id.equals(userId)) {
            binding.btnEditProfile.visibility = View.VISIBLE
            binding.btnEditProfile.setOnClickListener {
                val intent =
                    Intent(applicationContext, EditCongregationActivity::class.java)
                intent.putExtra(EditCongregationActivity.USER_ID_ARG, userData?.id)
                startActivity(intent)
            }
        }
    }
}