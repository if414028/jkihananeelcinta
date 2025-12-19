package com.jki.myhananeelcinta.pastoral

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jki.myhananeelcinta.R
import com.jki.myhananeelcinta.databinding.ActivityGreenRoomBinding
import com.jki.myhananeelcinta.model.Role
import com.jki.myhananeelcinta.pastoral.announcement.AnnouncementListActivity
import com.jki.myhananeelcinta.pastoral.birthday.BirthdayListActivity
import com.jki.myhananeelcinta.pastoral.congregation.CongregationListActivity
import com.jki.myhananeelcinta.pastoral.pastormessages.PastorMessagesListActivity
import com.jki.myhananeelcinta.pastoral.pastors.PastorListActivity

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
        binding.btnBirthdayList.setOnClickListener {
            val intent = Intent(applicationContext, BirthdayListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getTotalCongregation() {
        userRef.orderByChild("role").equalTo(Role.JEMAAT.role)
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