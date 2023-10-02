package com.jki.hananeelcinta.pastoral

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityGreenRoomBinding
import com.jki.hananeelcinta.pastoral.announcement.AnnouncementListActivity
import com.jki.hananeelcinta.pastoral.congregation.CongregationListActivity
import com.jki.hananeelcinta.pastoral.pastormessages.CreatePastorMessagesActivity
import com.jki.hananeelcinta.pastoral.pastormessages.PastorMessagesListActivity
import com.jki.hananeelcinta.pastoral.pastors.PastorListActivity

class GreenRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGreenRoomBinding

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val userRef: DatabaseReference = databaseReference.child("users")

    private var totalCongregation = 0
    private var totalPastor = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_green_room)
        supportActionBar?.hide()

        getTotalCongregation()
        getTotalPastor()
        setupLayout()
    }

    private fun setupLayout() {
        binding.btnBack.setOnClickListener { onBackPressed() }
        binding.btnWeeklyReflectionList.setOnClickListener {
            val intent = Intent(applicationContext, PastorMessagesListActivity::class.java)
            startActivity(intent)
        }
        binding.btnCongregationList.setOnClickListener {
            val intent = Intent(applicationContext, CongregationListActivity::class.java)
            startActivity(intent)
        }
        binding.btnPastorList.setOnClickListener {
            val intent = Intent(applicationContext, PastorListActivity::class.java)
            startActivity(intent)
        }
        binding.btnAnnouncementList.setOnClickListener {
            val intent = Intent(applicationContext, AnnouncementListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getTotalCongregation() {
        userRef.orderByChild("role").equalTo("jemaat")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        totalCongregation = snapshot.childrenCount.toInt()
                        binding.tvTotalCongregation.text = totalCongregation.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getTotalPastor() {
        userRef.orderByChild("role").equalTo("pastor")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        totalPastor = snapshot.childrenCount.toInt()
                        binding.tvTotalPastor.text = totalPastor.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

}