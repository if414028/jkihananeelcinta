package com.jki.hananeelcinta.pastoral.announcement

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityAnnouncementListBinding

class AnnouncementListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnnouncementListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_announcement_list)
        supportActionBar?.hide()
    }
}