package com.jki.hananeelcinta.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityForgotPasswordBinding
import com.jki.hananeelcinta.home.MainActivity
import com.jki.hananeelcinta.util.UIHelper


class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password)
        supportActionBar?.hide()
        firebaseAuth = FirebaseAuth.getInstance()
        setupLayout()
    }

    private fun setupLayout() {
        binding.tvBack.setOnClickListener { onBackPressed() }
        binding.btnResetPassword.setOnClickListener {
            resetPassword()
        }
    }

    private fun resetPassword() {
        binding.loading.visibility = View.VISIBLE
        val email = binding.etEmail.text.toString()
        if (email.isNotBlank()) {
            firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener {
                binding.loading.visibility = View.GONE
                UIHelper.getInstance().displaySuccessDialog(
                    resources.getString(R.string.reset_password_message),
                    resources.getString(R.string.reset_password_message_desc),
                    this,
                    {
                        onBackPressed()
                    },
                    resources.getString(R.string.OK),
                    false,
                )
            }.addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(this, "Email tidak boleh kosong.", Toast.LENGTH_SHORT)
                .show()
        }
    }
}