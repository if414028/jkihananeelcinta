package com.jki.myhananeelcinta.prayerrequest

import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.FirebaseDatabase
import com.jki.myhananeelcinta.R
import com.jki.myhananeelcinta.databinding.ActivityCreatePrayerRequestBinding
import com.jki.myhananeelcinta.model.PrayType
import com.jki.myhananeelcinta.model.PrayerRequest
import com.jki.myhananeelcinta.model.Status
import com.jki.myhananeelcinta.util.UIHelper
import com.jki.myhananeelcinta.util.UserConfiguration

class CreatePrayerRequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePrayerRequestBinding

    private val userData = UserConfiguration.getInstance().getUserData()
    private var databaseReference = FirebaseDatabase.getInstance().getReference("prayerRequest")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_prayer_request)
        supportActionBar?.hide()

        setupLayout()
    }

    private fun setupLayout() {
        binding.btnBack.setOnClickListener { onBackPressed() }
        setupRequestType()
        binding.btnSubmit.setOnClickListener { submitPrayRequest() }
    }

    private fun setupRequestType() {
        enumValues<PrayType>().forEach {
            val color = ContextCompat.getColor(this, R.color.color_primary)
            val rbPrayType = RadioButton(applicationContext)
            rbPrayType.text = it.type
            rbPrayType.setTextColor(resources.getColor(R.color.black))
            rbPrayType.buttonTintList = ColorStateList.valueOf(color)
            binding.rbPrayerType.addView(rbPrayType)
        }
        (binding.rbPrayerType.getChildAt(0) as RadioButton).isChecked = true
    }

    private fun getSelectedRequestType(): String {
        val checkedRadioButton =
            binding.root.findViewById<RadioButton>(binding.rbPrayerType.checkedRadioButtonId)
        return checkedRadioButton.text.toString()
    }

    private fun submitPrayRequest() {
        val prayRequest = PrayerRequest()
        prayRequest.prayDesc = binding.etRequestDescription.text.toString()
        prayRequest.prayType = getSelectedRequestType()
        prayRequest.status = Status.OPEN.name

        prayRequest.requesterId = userData!!.id
        prayRequest.requesterName = userData.fullName

        prayRequest.id = System.currentTimeMillis().toString()

        databaseReference.child(prayRequest.id!!).setValue(prayRequest)
            .addOnSuccessListener {
                showSuccessDialog()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal Mengirim Data", Toast.LENGTH_LONG).show()
            }
    }

    private fun showSuccessDialog() {
        UIHelper.getInstance().displaySuccessDialog(
            resources.getString(R.string.success_create_pastor_messages),
            resources.getString(R.string.success_create_pastor_message_desc),
            this,
            {
                finish()
            },
            resources.getString(R.string.OK),
            false,
        )
    }
}