package com.jki.hananeelcinta.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.R.*
import com.jki.hananeelcinta.databinding.ActivityLoginBinding
import com.jki.hananeelcinta.home.MainActivity
import com.jki.hananeelcinta.model.User
import com.jki.hananeelcinta.register.RegisterActivity
import com.jki.hananeelcinta.util.UserConfiguration


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    val database = FirebaseDatabase.getInstance()
    val usersRef = database.getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        supportActionBar?.hide()
        firebaseAuth = FirebaseAuth.getInstance()

        setupLayout()
    }

    private fun setupLayout() {
        binding.tvRegister.setOnClickListener {
            val intent = Intent(applicationContext, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            signIn()
        }

        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(applicationContext, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.btnTogglePassword.setOnClickListener { togglePassword() }
    }

    private fun togglePassword() {
        val inputType = binding.etPassword.inputType
        //129 == input type textPassword, not registered in InputType class
        if (inputType == 129) {
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT
            binding.btnTogglePassword.setImageResource((drawable.ic_eye_open))
        } else {
            binding.etPassword.inputType = 129
            binding.btnTogglePassword.setImageResource(R.drawable.ic_eye_closed)
        }
        binding.etPassword.setSelection(binding.etPassword.length());
    }

    private fun showLoading(isVisible: Boolean) {
        if (isVisible) {
            binding.loading.visibility = View.VISIBLE
            binding.lottieAnimation.playAnimation()
        } else {
            binding.lottieAnimation.cancelAnimation()
            binding.loading.visibility = View.GONE
        }
    }

    private fun showSuccessAnimation() {
        binding.successAnimation.visibility = View.VISIBLE
        binding.lottieSuccessAnimation.playAnimation()
    }

    private fun signIn() {
        showLoading(true)
        val email = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()

        if (email.isBlank() || password.isBlank()) {
            showLoading(false)
            Toast.makeText(this, "Username atau Password tidak boleh kosong", Toast.LENGTH_SHORT)
                .show()
            return
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    firebaseAuth.currentUser?.let { userId -> saveUserData(userId.uid) }
                } else {
                    showLoading(false)
                    if (it.exception is FirebaseAuthInvalidCredentialsException) {
                        when ((it.exception as FirebaseAuthInvalidCredentialsException).errorCode) {
                            "ERROR_USER_NOT_FOUND" -> Toast.makeText(
                                this,
                                "User tidak ditemukan",
                                Toast.LENGTH_SHORT
                            ).show()

                            "ERROR_INVALID_EMAIL" -> Toast.makeText(
                                this,
                                "Format email salah",
                                Toast.LENGTH_SHORT
                            ).show()

                            "ERROR_WRONG_PASSWORD" -> Toast.makeText(
                                this,
                                "Password Salah",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else if (it.exception is FirebaseAuthInvalidUserException) {
                        when ((it.exception as FirebaseAuthInvalidUserException).errorCode) {
                            "ERROR_USER_NOT_FOUND" -> Toast.makeText(
                                this,
                                "User tidak ditemukan",
                                Toast.LENGTH_SHORT
                            ).show()

                            "ERROR_INVALID_EMAIL" -> Toast.makeText(
                                this,
                                "Format email salah",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
    }

    private fun saveUserData(userId: String) {
        usersRef.child(userId).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(User::class.java)
                        user?.let {
                            showLoading(false)
                            showSuccessAnimation()

                            Handler().postDelayed({
                                UserConfiguration.getInstance().setUserId(userId)
                                UserConfiguration.getInstance().setUserData(it)
                                val intent = Intent(applicationContext, MainActivity::class.java)
                                startActivity(intent)
                            }, 1000)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showLoading(false)
                    Toast.makeText(
                        applicationContext,
                        "Gagal mendapatkan data user",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}