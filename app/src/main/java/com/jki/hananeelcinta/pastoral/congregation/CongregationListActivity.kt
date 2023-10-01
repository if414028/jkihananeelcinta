package com.jki.hananeelcinta.pastoral.congregation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityCongregationListBinding
import com.jki.hananeelcinta.databinding.ItemUserBinding
import com.jki.hananeelcinta.model.User
import com.jki.hananeelcinta.util.SimpleFilterRecyclerAdapter
import com.jki.hananeelcinta.util.SimpleRecyclerAdapter
import java.util.ArrayList

class CongregationListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCongregationListBinding
    private lateinit var adapter: SimpleFilterRecyclerAdapter<User>

    private val database = FirebaseDatabase.getInstance()
    private val userRef = database.getReference("users")
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference

    private var userList: ArrayList<User> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_congregation_list)
        supportActionBar?.hide()

        setupLayout()
        getUserList()
    }

    private fun setupLayout() {
        binding.btnBack.setOnClickListener { onBackPressed() }
        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.rvCongregation.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter = SimpleFilterRecyclerAdapter(
            arrayListOf(),
            R.layout.item_user,
            { holder, item ->
                val itemBinding: ItemUserBinding = holder?.layoutBinding as ItemUserBinding
                itemBinding.tvUserName.text = item?.fullName
                itemBinding.tvUserPhone.text = item?.phoneNumber
                itemBinding.tvDateOfBirth.text = item?.dateOfBirth
                getProfileImage(item.id, itemBinding.ivProfile)
            }, object : SimpleFilterRecyclerAdapter.OnSearchListener<User> {
                override fun onSearchRules(model: User?, searchedText: String?): User {
                    TODO("Not yet implemented")
                }

                override fun onSearch(model: ArrayList<User>?) {
                    TODO("Not yet implemented")
                }

                override fun onSearchEmpty(searchedText: String?) {
                    TODO("Not yet implemented")
                }
            })
        binding.rvCongregation.adapter = adapter
    }

    private fun getUserList() {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        var user = dataSnapshot.getValue(User::class.java)
                        if (user != null) {
                            userList.add(user)
                        }
                    }
                    adapter.mainData = userList
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun getProfileImage(userId: String, ivProfile: ImageView) {
        val profilePicturesRef = storageRef.child("${userId}/profile-pictures")
        profilePicturesRef.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.getResult()
                val imageUrl = downloadUri.toString()
                val requestOptions = RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both the original and resized image
                    .centerCrop() // Center-crop the image to fit the ImageView

                Glide.with(applicationContext)
                    .load(imageUrl)
                    .placeholder(R.drawable.hancin_logo)
                    .apply(requestOptions)
                    .into(ivProfile)
            }
        }
    }
}