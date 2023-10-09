package com.jki.hananeelcinta.pastoral.announcement

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityCreateAnnouncementBinding
import com.jki.hananeelcinta.model.Announcement
import com.jki.hananeelcinta.util.PictureUploader
import com.jki.hananeelcinta.util.UIHelper
import java.util.UUID

class CreateAnnouncementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAnnouncementBinding

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    private var model = Announcement()

    private var databaseReference = FirebaseDatabase.getInstance().getReference("announcement")
    private val announcementPictureUploader = PictureUploader()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_announcement)
        supportActionBar?.hide()

        setupLayout()
    }

    private fun setupLayout() {
        binding.btnBack.setOnClickListener { onBackPressed() }
        binding.btnAddImage.setOnClickListener { openGallery() }
        binding.btnSubmit.setOnClickListener { invalidateData() }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            PICK_IMAGE_REQUEST
        )
    }

    private fun invalidateData() {
        if (binding.etAnnouncementTitle.text.isNullOrEmpty()) {
            binding.tvAnnouncementTitleError.visibility = View.VISIBLE
        }
        if (binding.etAnnouncementValue.text.isNullOrEmpty()) {
            binding.tvAnnouncementValueError.visibility = View.VISIBLE
        }

        if (!binding.etAnnouncementTitle.text.isNullOrEmpty()
            && !binding.etAnnouncementValue.text.isNullOrEmpty()
        ) {
            uploadImage()
        }
    }

    private fun submit() {
        model.title = binding.etAnnouncementTitle.text.toString()
        model.desc = binding.etAnnouncementValue.text.toString()
        model.date = System.currentTimeMillis()
        model.infoUrl = binding.etAnnoucementLink.text.toString()

        databaseReference.child(model.date.toString()).setValue(model)
            .addOnSuccessListener {
                showSuccessDialog()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal Mengirim Data", Toast.LENGTH_LONG)
            }
    }

    private fun showSuccessDialog() {
        UIHelper.getInstance().displaySuccessDialog(
            resources.getString(R.string.success_submit_announcement_title),
            resources.getString(R.string.success_submit_announcement_sub_title),
            this,
            {
                finish()
            },
            resources.getString(R.string.OK),
            false,
        )
    }

    private fun uploadImage() {
        if (selectedImageUri != null) {
            announcementPictureUploader.uploadAnnouncementPicture(selectedImageUri!!) { imageUrl, error ->
                if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                } else {
                    if (imageUrl != null) {
                        model.imageUrl = imageUrl
                        submit()
                    }
                }
            }
        } else {
            submit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data

            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both the original and resized image

            Glide.with(applicationContext)
                .load(selectedImageUri)
                .placeholder(R.drawable.ic_no_profile_image)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .apply(requestOptions)
                .into(binding.ivAnnouncement)

            binding.btnAddImage.visibility = View.GONE
            binding.ivAnnouncement.visibility = View.VISIBLE
        }
    }
}