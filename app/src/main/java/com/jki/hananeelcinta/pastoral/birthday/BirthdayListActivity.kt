package com.jki.hananeelcinta.pastoral.birthday

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.jki.hananeelcinta.databinding.ActivityBirthdayListBinding
import com.jki.hananeelcinta.databinding.ItemBirthdayBinding
import com.jki.hananeelcinta.databinding.ItemUserBinding
import com.jki.hananeelcinta.model.User
import com.jki.hananeelcinta.pastoral.congregation.CongregationDetailActivity
import com.jki.hananeelcinta.util.SimpleFilterRecyclerAdapter
import com.jki.hananeelcinta.util.SimpleRecyclerAdapter
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BirthdayListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBirthdayListBinding

    private lateinit var adapter: SimpleRecyclerAdapter<User>

    private val database = FirebaseDatabase.getInstance()
    private val userRef = database.getReference("users")
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference

    private var filteredUserList: ArrayList<User> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_birthday_list)
        supportActionBar?.hide()

        setupLayout()
        getUserList()
    }

    private fun setupLayout() {
        binding.btnBack.setOnClickListener { onBackPressed() }
        initRecyclerView()

    }

    private fun initRecyclerView() {
        binding.rvBirthday.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        adapter = SimpleRecyclerAdapter(
            arrayListOf(),
            R.layout.item_birthday
        ) { holder, item ->
            val itemBinding: ItemBirthdayBinding = holder?.layoutBinding as ItemBirthdayBinding
            itemBinding.tvUserName.text = item?.fullName
            itemBinding.tvDateOfBirth.text = item?.dateOfBirth
            getProfileImage(item.id, itemBinding.ivProfile)

            val dateOfBirth = item?.dateOfBirth?.let { parseDateOfBirth(it) }
            dateOfBirth?.let {
                val age = calculateAge(it)
                itemBinding.tvAge.text = age
            } ?: run {
                itemBinding.tvAge.text = "invalid date format"
            }

            itemBinding.root.setOnClickListener {
                val intent =
                    Intent(applicationContext, CongregationDetailActivity::class.java)
                intent.putExtra(CongregationDetailActivity.USER_ID_ARG, item.id)
                startActivity(intent)
            }
        }
        binding.rvBirthday.adapter = adapter
    }

    private fun getUserList() {
        binding.isLoading = true
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userList = mutableListOf<User>()
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        user?.let {
                            userList.add(it)
                        }
                    }
                    val usersWithBirthday = getUsersWithBirthdaysThisMonth(userList)
                    filteredUserList = usersWithBirthday as ArrayList<User>
                    binding.isLoading = false

                    if (filteredUserList.isNotEmpty()) {
                        adapter.mainData = sortUsersByBirthday(filteredUserList)
                    } else {
                        binding.isEmpty = true
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.isLoading = false
                binding.isError = true
            }

        })
    }

    fun getUsersWithBirthdaysThisMonth(userList: List<User>): List<User> {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

        return userList.filter { user ->
            val dateOfBirth = parseDateOfBirth(user.dateOfBirth)
            dateOfBirth?.let {
                val birthMonth = Calendar.getInstance().apply { time = it }.get(Calendar.MONTH)
                birthMonth == currentMonth
            } ?: false
        }
    }

    private fun parseDateOfBirth(dateOfBirth: String): Date? {
        return try {
            val format = SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH)
            format.parse(dateOfBirth)
        } catch (e: Exception) {
            null
        }
    }

    private fun isBirthdayWithinNextMonth(dateOfBirth: Date): Boolean {
        val now = Calendar.getInstance()
        val oneMonthAhead = getEndDateOneMonthAhead()

        val dobCalendar = Calendar.getInstance().apply {
            time = dateOfBirth
        }

        val currentMonth = now.get(Calendar.MONTH)
        val currentDay = now.get(Calendar.DAY_OF_MONTH)
        val nextMonth = oneMonthAhead.get(Calendar.MONTH)
        val nextDay = oneMonthAhead.get(Calendar.DAY_OF_MONTH)

        return (dobCalendar.get(Calendar.MONTH) == currentMonth && dobCalendar.get(Calendar.DAY_OF_MONTH) >= currentDay) ||
                (dobCalendar.get(Calendar.MONTH) == nextMonth && dobCalendar.get(Calendar.DAY_OF_MONTH) <= nextDay)
    }

    private fun getEndDateOneMonthAhead(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 1)
        return calendar
    }

    private fun sortUsersByBirthday(userList: List<User>): List<User> {
        return userList.sortedWith(compareBy(
            { user ->
                val dateOfBirth = parseDateOfBirth(user.dateOfBirth)
                val calendar = Calendar.getInstance()
                dateOfBirth?.let {
                    calendar.time = it
                    calendar.get(Calendar.MONTH) // Urutkan berdasarkan bulan
                }
            },
            { user ->
                val dateOfBirth = parseDateOfBirth(user.dateOfBirth)
                val calendar = Calendar.getInstance()
                dateOfBirth?.let {
                    calendar.time = it
                    calendar.get(Calendar.DAY_OF_MONTH) // Urutkan berdasarkan hari
                }
            }
        ))
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

    fun calculateAge(dateOfBirth: Date): String {
        val birthCalendar = Calendar.getInstance().apply {
            time = dateOfBirth
        }

        val today = Calendar.getInstance()

        var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        return "$age Tahun"
    }
}