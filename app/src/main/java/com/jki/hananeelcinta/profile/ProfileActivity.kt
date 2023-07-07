package com.jki.hananeelcinta.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityProfileBinding
import com.jki.hananeelcinta.login.LoginActivity
import com.jki.hananeelcinta.util.UserConfiguration

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth

    val userData = UserConfiguration.getInstance().getUserData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)
        supportActionBar?.hide()

        setupLayout()
    }

    private fun setupLayout() {
        if (userData != null) {
            binding.username = userData.username
            binding.nij = userData.id
        }
        binding.btnSignOut.setOnClickListener { signOut() }
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
}