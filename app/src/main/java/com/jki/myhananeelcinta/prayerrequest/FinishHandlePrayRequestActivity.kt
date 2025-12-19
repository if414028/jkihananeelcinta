package com.jki.myhananeelcinta.prayerrequest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.FirebaseDatabase
import com.jki.myhananeelcinta.R
import com.jki.myhananeelcinta.databinding.ActivityFinishHandlePrayRequestBinding
import com.jki.myhananeelcinta.model.PrayerRequest
import com.jki.myhananeelcinta.model.Status
import com.jki.myhananeelcinta.util.UIHelper

class FinishHandlePrayRequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFinishHandlePrayRequestBinding

    private var prayRequest = PrayerRequest()
    private val database = FirebaseDatabase.getInstance()
    private val prayRequestDatabaseReference = database.getReference("prayerRequest")

    companion object {
        const val HANDLE_PRAY_REQUEST = 801
        const val SUCCESS_HANDLE_PRAY_RESULT = 800
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_finish_handle_pray_request)

        getAdditionalData()
        setupLayout()
    }

    private fun getAdditionalData() {
        val intent = intent
        if (intent != null) {
            if (intent.hasExtra("prayRequest")) {
                prayRequest = intent.getParcelableExtra("prayRequest")!!
            }
        }
    }

    private fun setupLayout() {
        binding.btnBack.setOnClickListener { onBackPressed() }
        binding.btnSubmit.setOnClickListener {
            prayRequest.status = Status.DONE.toString()
            prayRequest.prayResult = binding.etPrayResult.text.toString()
            prayRequest.id?.let { it1 ->
                prayRequestDatabaseReference.child(it1).setValue(prayRequest)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            showSuccessDialog()
                        } else {
                            Toast.makeText(
                                this,
                                "Gagal memperbaharui data, silahkan coba lagi.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

    private fun showSuccessDialog() {
        UIHelper.getInstance().displaySuccessDialog(
            resources.getString(R.string.done_handle_pray_request),
            resources.getString(R.string.success_handle_pray_request_desc),
            this,
            {
                setResult(SUCCESS_HANDLE_PRAY_RESULT)
                finish()
            },
            resources.getString(R.string.OK),
            true,
        )
    }
}