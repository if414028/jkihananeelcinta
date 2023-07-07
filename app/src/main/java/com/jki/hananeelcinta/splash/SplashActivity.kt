package com.jki.hananeelcinta.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jki.hananeelcinta.home.MainActivity
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivitySplashBinding
import com.jki.hananeelcinta.login.LoginActivity
import com.jki.hananeelcinta.model.User
import com.jki.hananeelcinta.util.UserConfiguration

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var firebaseAuth: FirebaseAuth
    val database = FirebaseDatabase.getInstance()
    val usersRef = database.getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        firebaseAuth = FirebaseAuth.getInstance()
        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed(
            {
                checkAuthStatus()
            },
            5000
        )
    }

    private fun checkAuthStatus() {
        if (firebaseAuth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } else {
            getUserData()
        }
    }

    private fun getUserData() {
        firebaseAuth.currentUser?.let { currentUser ->
            val userId = firebaseAuth.currentUser!!.uid
            usersRef.child(currentUser.uid).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val user = snapshot.getValue(User::class.java)
                            user?.let { userData ->
                                UserConfiguration.getInstance().setUserId(userId)
                                UserConfiguration.getInstance().setUserData(userData)
                                val intent = Intent(applicationContext, MainActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        val intent = Intent(applicationContext, LoginActivity::class.java)
                        startActivity(intent)
                    }
                }
            )
        }
    }
}