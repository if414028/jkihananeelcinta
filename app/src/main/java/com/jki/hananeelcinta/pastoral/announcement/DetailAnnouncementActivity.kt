package com.jki.hananeelcinta.pastoral.announcement

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.database.FirebaseDatabase
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityDetailAnnouncementBinding
import com.jki.hananeelcinta.model.Announcement
import com.jki.hananeelcinta.model.Role
import com.jki.hananeelcinta.util.UIHelper
import com.jki.hananeelcinta.util.UserConfiguration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailAnnouncementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailAnnouncementBinding
    private var model = Announcement()

    private var databaseReference = FirebaseDatabase.getInstance().getReference("announcement")

    private val isAdmin =
        UserConfiguration.getInstance().getUserData()?.role.equals(Role.SUPERUSER.role)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_announcement)
        supportActionBar?.hide()
        getAdditionalData()
        setupLayout()
    }

    private fun getAdditionalData() {
        val intent = intent
        if (intent != null) {
            if (intent.hasExtra("announcement")) {
                model = intent.getParcelableExtra("announcement")!!
            }
        }
    }

    private fun convertTimestampToReadableDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }

    private fun setupLayout() {
        binding.btnBack.setOnClickListener { onBackPressed() }
        binding.tvAnnouncementTitle.text = model.title
        binding.tvAnnouncementTime.text = convertTimestampToReadableDate(model.date)
        binding.tvDetailAnnouncement.text = model.desc
        binding.tvDetailAnnouncement.movementMethod = LinkMovementMethod.getInstance()

        if (model.infoUrl?.isNotEmpty() == true) {
            binding.tvLinkTitle.visibility = View.VISIBLE
            binding.tvAnnouncementLink.visibility = View.VISIBLE
            binding.tvAnnouncementLink.text = model.infoUrl
            binding.tvAnnouncementLink.movementMethod = LinkMovementMethod.getInstance()
        }

        if (model.contactPersonName?.isNotEmpty() == true) {
            binding.tvContactPersonName.visibility = View.VISIBLE
            binding.tvContactPersonName.text = model.contactPersonName
        }

        if (model.contactPerson?.isNotEmpty() == true) {
            binding.tvContactPersonTitle.visibility = View.VISIBLE
            binding.tvContactPerson.visibility = View.VISIBLE
            binding.tvContactPerson.text = model.contactPerson
            binding.tvContactPerson.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                val message = getString(R.string.announcement_message, model.title)
                intent.data =
                    Uri.parse("https://api.whatsapp.com/send?phone=${model.contactPerson}&text=$message")
                startActivity(intent)
            }
        }

        if (model.imageUrl.toString().isNotEmpty()) {
            Glide.with(applicationContext)
                .load(model.imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.ivAnnouncement)
        }

        if (isAdmin) {
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnDelete.setOnClickListener {
                UIHelper.getInstance().displayConfirmation(
                    resources.getString(R.string.confirm_delete_announcement),
                    resources.getString(R.string.confirm_delete_announcement_desc),
                    this,
                    { },
                    {
                        deleteAnnouncement()
                    },
                    resources.getString(R.string.text_confirmation_no),
                    resources.getString(R.string.text_confirmation_yes_),
                    R.drawable.question,
                    true
                )
            }
        }
    }

    private fun deleteAnnouncement() {
        databaseReference.child(model.date.toString()).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                UIHelper.getInstance().displaySuccessDialog(
                    resources.getString(R.string.success_delete_announcement),
                    null,
                    this,
                    {
                        finish()
                    },
                    resources.getString(R.string.OK),
                    false,
                )
            } else {
                Toast.makeText(this, "Gagal menghapus pengumuman", Toast.LENGTH_SHORT).show()
            }
        }
    }
}