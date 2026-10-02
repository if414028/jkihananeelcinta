package com.jki.myhananeelcinta.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.jki.myhananeelcinta.BuildConfig
import com.jki.myhananeelcinta.R
import com.jki.myhananeelcinta.auth.CmsAuthRepository
import com.jki.myhananeelcinta.databinding.ActivitySplashBinding
import com.jki.myhananeelcinta.home.MainActivity
import com.jki.myhananeelcinta.login.LoginActivity
import com.jki.myhananeelcinta.util.UserConfiguration

class SplashActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivitySplashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        supportActionBar?.hide()
        binding.tvVersion.text = "Version ${BuildConfig.VERSION_NAME}"
        handler.postDelayed({ checkAuthStatus() }, 5000)
    }

    private fun checkAuthStatus() {
        UserConfiguration.getInstance().clearSession()
        if (FirebaseAuth.getInstance().currentUser == null) {
            navigate(false)
            return
        }
        val repository = CmsAuthRepository.getInstance(this)
        repository.openSession { result ->
            if (isFinishing || isDestroyed) return@openSession
            result.fold({ user ->
                repository.saveSession(user)
                navigate(true)
            }, { error ->
                repository.handleFailure(error)
                Toast.makeText(this, error.message ?: "Gagal mengambil profil CMS.", Toast.LENGTH_LONG).show()
                navigate(false)
            })
        }
    }

    private fun navigate(authenticated: Boolean) {
        startActivity(Intent(this, if (authenticated) MainActivity::class.java else LoginActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
