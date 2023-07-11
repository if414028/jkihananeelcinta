package com.jki.hananeelcinta.services

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityServicesBinding

class ServicesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServicesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_services)
        supportActionBar?.hide()
        setupLayout()
    }

    private fun setupLayout() {
        binding.btnBack.setOnClickListener { onBackPressed() }
        binding.btnJadwaMk.setOnClickListener {
            val intent = Intent(applicationContext, MezbahKeluargaActivity::class.java)
            startActivity(intent)
        }
    }
}