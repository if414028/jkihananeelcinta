package com.jki.hananeelcinta.pastoral.congregation

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityCongregationListBinding
import com.jki.hananeelcinta.databinding.ItemUserBinding
import com.jki.hananeelcinta.model.User
import com.jki.hananeelcinta.util.SimpleFilterRecyclerAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        binding.etSearch.addTextChangedListener {
            val searchedText = it.toString()
            adapter.filter(searchedText)
        }
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
                itemBinding.tvUserProfession.text = item?.job
                itemBinding.tvDateOfBirth.text = countUserAge(item?.dateOfBirth)
                getProfileImage(item.id, itemBinding.ivProfile)

                itemBinding.root.setOnClickListener {
                    val intent =
                        Intent(applicationContext, CongregationDetailActivity::class.java)
                    intent.putExtra(CongregationDetailActivity.USER_ID_ARG, item.id)
                    startActivity(intent)
                }
            }, object : SimpleFilterRecyclerAdapter.OnSearchListener<User> {
                override fun onSearchRules(model: User?, searchedText: String?): User? {
                    if (searchedText?.let {
                            model?.fullName.toString().lowercase(Locale.getDefault())
                                .contains(it)
                        } == true || searchedText?.let {
                            model?.username.toString().lowercase(Locale.getDefault())
                                .contains(it)
                        } == true) {
                        showUserNotFoundLayout(false)
                        return model!!
                    }
                    return null
                }

                override fun onSearch(model: ArrayList<User>?) {
                }

                override fun onSearchEmpty(searchedText: String?) {
                    showUserNotFoundLayout(true)
                }
            })
        binding.rvCongregation.adapter = adapter
    }

    private fun showUserNotFoundLayout(show: Boolean) {
        binding.lyUserNotFound.tvTitle.text =
            resources.getString(R.string.congregation_not_found_title)
        binding.lyUserNotFound.tvInfo.text =
            resources.getString(R.string.congregation_not_found_sub_title)
        binding.isUserNotFound = show
    }

    private fun getUserList() {
        binding.isLoading = true
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val tempList: ArrayList<User> = arrayListOf()
                    for (dataSnapshot in snapshot.children) {
                        var user = dataSnapshot.getValue(User::class.java)
                        if (user != null) {
                            tempList.add(user)
                        }
                    }
                    userList = tempList
                    binding.isLoading = false
                    if (userList.isNotEmpty()) {
                        adapter.mainData = userList
                    } else {
                        binding.isError = true
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.isLoading = false
                binding.isError = true
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
                    .placeholder(R.drawable.ic_no_profile_image)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .apply(requestOptions)
                    .into(ivProfile)
            }
        }
    }

    private fun countUserAge(dateOfBirth: String?): String {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.US)
        val dob = dateOfBirth?.let { dateFormat.parse(it) }
        val today = Date()

        var age = today.year - (dob?.year ?: 0)
        if (today.before(dob)) {
            age--
        }

        return "$age Tahun"
    }
}