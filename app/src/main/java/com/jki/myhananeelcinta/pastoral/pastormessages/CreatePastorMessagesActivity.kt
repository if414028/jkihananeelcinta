package com.jki.myhananeelcinta.pastoral.pastormessages

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.jki.myhananeelcinta.R
import com.jki.myhananeelcinta.databinding.ActivityCreatePastorMessagesBinding
import com.jki.myhananeelcinta.model.PastorMessage
import com.jki.myhananeelcinta.util.UIHelper


class CreatePastorMessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePastorMessagesBinding
    private var databaseReference = FirebaseDatabase.getInstance().getReference("pastorMessages")
    private val user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_pastor_messages)
        supportActionBar?.hide()

        setupLayout()
    }

    private fun setupLayout() {
        binding.btnBack.setOnClickListener { onBackPressed() }
        binding.btnSubmit.setOnClickListener { submitMessages() }
    }

    private fun submitMessages() {
        val pastorMessage = PastorMessage()
        pastorMessage.date = System.currentTimeMillis()
        pastorMessage.title = binding.etTitle.text.toString()
        pastorMessage.messages = binding.etMessages.text.toString()
        pastorMessage.writer = binding.etWriter.text.toString()

        databaseReference.child(pastorMessage.date.toString()).setValue(pastorMessage)
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