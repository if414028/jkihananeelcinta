package com.jki.hananeelcinta.reflection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jki.hananeelcinta.R

class ReflectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reflection)
        supportActionBar?.hide()
    }
}