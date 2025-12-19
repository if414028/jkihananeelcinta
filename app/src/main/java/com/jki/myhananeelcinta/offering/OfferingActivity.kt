package com.jki.myhananeelcinta.offering

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.jki.myhananeelcinta.R
import com.jki.myhananeelcinta.databinding.ActivityOfferingBinding

class OfferingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOfferingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_offering)
        supportActionBar?.hide()

        setupLayout()
    }

    private fun setupLayout() {
        binding.btnBack.setOnClickListener { onBackPressed() }
        binding.btnCopyBankAccount.setOnClickListener {
            copyBankAccount("bankAccount", resources.getString(R.string.bank_account_plain))
        }
    }

    private fun copyBankAccount(label: String, text: String) {
        val clipboardManager =
            applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(label, text)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(applicationContext, "Nomor rekening berhasil disalin.", Toast.LENGTH_SHORT)
            .show()
    }

}