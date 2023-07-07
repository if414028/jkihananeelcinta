package com.jki.hananeelcinta.login

import android.content.Intent
import android.os.Bundle
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
import com.jki.hananeelcinta.home.MainActivity
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityLoginBinding
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
    }

    private fun signIn() {
        val email = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Username atau Password tidak boleh kosong", Toast.LENGTH_SHORT)
                .show()
            return
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    firebaseAuth.currentUser?.let { userId -> saveUserData(userId.uid) }
                } else {
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
                            UserConfiguration.getInstance().setUserId(userId)
                            UserConfiguration.getInstance().setUserData(it)
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        applicationContext,
                        "Gagal mendapatkan data user",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }
}