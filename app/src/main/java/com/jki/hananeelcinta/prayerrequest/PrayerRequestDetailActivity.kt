package com.jki.hananeelcinta.prayerrequest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityPrayerRequestDetailBinding

class PrayerRequestDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrayerRequestDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_prayer_request_detail)
        supportActionBar?.hide()
    }
}