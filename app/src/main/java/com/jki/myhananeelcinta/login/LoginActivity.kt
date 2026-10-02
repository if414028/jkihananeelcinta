package com.jki.myhananeelcinta.login

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.os.Handler
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.jki.myhananeelcinta.R
import com.jki.myhananeelcinta.auth.CmsAuthRepository
import com.jki.myhananeelcinta.R.*
import com.jki.myhananeelcinta.databinding.ActivityLoginBinding
import com.jki.myhananeelcinta.home.MainActivity
import com.jki.myhananeelcinta.register.RegisterActivity
import com.jki.myhananeelcinta.util.UserConfiguration


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var isSigningIn = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        supportActionBar?.hide()
        firebaseAuth = FirebaseAuth.getInstance()

        setupLayout()

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Tutup semua activity
                finishAffinity()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
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
        binding.btnLogin.isEnabled = !isVisible
        binding.tvRegister.isEnabled = !isVisible
        binding.tvForgotPassword.isEnabled = !isVisible
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
        if (isSigningIn) return
        val email = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Email atau password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }
        isSigningIn = true
        showLoading(true)
        UserConfiguration.getInstance().clearSession()
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                openCmsSession()
            } else {
                isSigningIn = false
                showLoading(false)
                val message = when (val error = task.exception) {
                    is FirebaseAuthInvalidUserException -> "Akun tidak tersedia atau dinonaktifkan."
                    is FirebaseAuthInvalidCredentialsException ->
                        if (error.errorCode == "ERROR_INVALID_EMAIL") "Format email salah" else "Email atau password salah"
                    else -> "Login Firebase gagal. Periksa koneksi lalu coba lagi."
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openCmsSession() {
        val repository = CmsAuthRepository.getInstance(this)
        repository.openSession { result ->
            if (isFinishing || isDestroyed) return@openSession
            isSigningIn = false
            showLoading(false)
            result.fold({ user ->
                repository.saveSession(user)
                showSuccessAnimation()
                handler.postDelayed({
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }, 1000)
            }, { error ->
                repository.handleFailure(error)
                Toast.makeText(this, error.message ?: "Gagal mengambil profil CMS.", Toast.LENGTH_LONG).show()
            })
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}