package com.jki.hananeelcinta.prayerrequest

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityPrayerRequestDetailBinding
import com.jki.hananeelcinta.model.PrayerRequest
import com.jki.hananeelcinta.model.Role
import com.jki.hananeelcinta.model.Status
import com.jki.hananeelcinta.model.User
import com.jki.hananeelcinta.util.UIHelper
import com.jki.hananeelcinta.util.UserConfiguration

class PrayerRequestDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrayerRequestDetailBinding


    private val database = FirebaseDatabase.getInstance()
    private val prayRequestDatabaseReference = database.getReference("prayerRequest")
    private val userDatabaseReference = database.getReference("users")

    private var isAdmin =
        UserConfiguration.getInstance().getUserData()?.role.equals(Role.SUPERUSER.role)

    private var prayRequest = PrayerRequest()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_prayer_request_detail)
        supportActionBar?.hide()

        getAdditionalData()
        setupLayout()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FinishHandlePrayRequestActivity.HANDLE_PRAY_REQUEST) {
            if (resultCode == FinishHandlePrayRequestActivity.SUCCESS_HANDLE_PRAY_RESULT) {
                finish()
            }
        }
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

        getRequesterData()
        setupPrayRequestData()
        if (prayRequest.status != null) {
            if (prayRequest.status.equals(Status.IN_PROGRESS.name) || prayRequest.status.equals(
                    Status.DONE.name
                )
            ) {
                if (prayRequest.handlerId != null) {
                    getPrayerData()
                }
            }
        }

        if (isAdmin) {
            when (prayRequest.status) {
                Status.OPEN.name -> {
                    binding.btnSubmit.visibility = View.VISIBLE
                    binding.btnSubmit.text = resources.getString(R.string.handle_pray_request)
                }

                Status.IN_PROGRESS.name -> {
                    binding.btnSubmit.visibility = View.VISIBLE
                    binding.btnSubmit.text = resources.getString(R.string.done)
                }

                else -> {
                    binding.btnSubmit.visibility = View.GONE
                }
            }
        } else {
            binding.btnSubmit.visibility = View.GONE
        }

        binding.btnSubmit.setOnClickListener {
            when (prayRequest.status) {
                Status.OPEN.name -> {
                    prayRequest.status = Status.IN_PROGRESS.toString()
                    prayRequest.handlerId = UserConfiguration.getInstance().getUserId()
                    prayRequest.handlerName = UserConfiguration.getInstance().getUserData()?.fullName
                    prayRequest.id?.let { it1 ->
                        prayRequestDatabaseReference.child(it1).setValue(prayRequest)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    updatePrayRequestData()
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

                Status.IN_PROGRESS.name -> {
                    val intent =
                        Intent(applicationContext, FinishHandlePrayRequestActivity::class.java)
                    intent.putExtra("prayRequest", prayRequest)
                    startActivityForResult(
                        intent,
                        FinishHandlePrayRequestActivity.HANDLE_PRAY_REQUEST
                    )
                }
            }
        }
    }

    private fun updatePrayRequestData() {
        binding.status = prayRequest.status
        binding.statusName = Status.valueOf(prayRequest.status!!).status
        binding.cardStatus.setCardBackgroundColor(resources.getColor(R.color.yellowC79C52))
        binding.btnSubmit.text = resources.getString(R.string.done)
        binding.btnSubmit.setOnClickListener {
            val intent =
                Intent(applicationContext, FinishHandlePrayRequestActivity::class.java)
            intent.putExtra("prayRequest", prayRequest)
            startActivityForResult(
                intent,
                FinishHandlePrayRequestActivity.HANDLE_PRAY_REQUEST
            )
        }

        getPrayerData()
    }

    private fun setupPrayRequestData() {
        binding.prayType = prayRequest.prayType
        binding.prayDesc = prayRequest.prayDesc
        binding.status = prayRequest.status
        binding.prayResult = prayRequest.prayResult

        when (prayRequest.status) {
            Status.OPEN.name -> {
                binding.cardStatus.setCardBackgroundColor(resources.getColor(R.color.greyAAAAAA))
                binding.statusName = Status.OPEN.status
            }

            Status.IN_PROGRESS.name -> {
                binding.cardStatus.setCardBackgroundColor(resources.getColor(R.color.yellowC79C52))
                binding.statusName = Status.IN_PROGRESS.status
            }

            Status.DONE.name -> {
                binding.cardStatus.setCardBackgroundColor(resources.getColor(R.color.green40BB74))
                binding.statusName = Status.DONE.status
            }

            Status.CANCELLED.name -> {
                binding.cardStatus.setCardBackgroundColor(resources.getColor(R.color.redD11008))
                binding.statusName = Status.CANCELLED.status
            }
        }
    }

    private fun getRequesterData() {
        prayRequest.requesterId?.let {
            userDatabaseReference.child(it)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)
                        binding.requesterName = user?.fullName
                        binding.requesterGender = user?.gender
                        binding.requesterAddress = user?.address
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.isError = true
                    }
                })
        }
    }

    private fun getPrayerData() {
        prayRequest.handlerId?.let {
            userDatabaseReference.child(it)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)
                        binding.prayerName = user?.fullName
                        binding.prayerGender = user?.gender
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.isError = true
                    }
                })
        }
    }

    private fun showSuccessDialog() {
        UIHelper.getInstance().displaySuccessDialog(
            resources.getString(R.string.success_handle_pray_request),
            resources.getString(R.string.success_handle_pray_request_desc),
            this,
            {},
            resources.getString(R.string.OK),
            true,
        )
    }

}