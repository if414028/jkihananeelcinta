package com.jki.hananeelcinta.reflection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityReflectionBinding

class ReflectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReflectionBinding

    companion object {
        const val DETAIL_MESSAGE = "detailMessage"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reflection)
        supportActionBar?.hide()
    }

    private fun getAdditionalData() {

    }
}