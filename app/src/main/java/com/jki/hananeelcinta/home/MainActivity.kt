package com.jki.hananeelcinta.home

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityMainBinding
import com.jki.hananeelcinta.databinding.ItemMenuBinding
import com.jki.hananeelcinta.home.weeklyreflection.DetailWeeklyReflectionFragment
import com.jki.hananeelcinta.model.Announcement
import com.jki.hananeelcinta.model.PastorMessage
import com.jki.hananeelcinta.model.Role
import com.jki.hananeelcinta.offering.OfferingActivity
import com.jki.hananeelcinta.pastoral.GreenRoomActivity
import com.jki.hananeelcinta.pastoral.announcement.DetailAnnouncementActivity
import com.jki.hananeelcinta.pastoral.pastormessages.PastorMessagesListActivity
import com.jki.hananeelcinta.prayerrequest.CreatePrayerRequestActivity
import com.jki.hananeelcinta.prayerrequest.PrayerRequestListActivity
import com.jki.hananeelcinta.profile.ProfileActivity
import com.jki.hananeelcinta.reflection.ReflectionActivity
import com.jki.hananeelcinta.services.ServicesActivity
import com.jki.hananeelcinta.util.ImageSliderAdapter
import com.jki.hananeelcinta.util.SimpleRecyclerAdapter
import com.jki.hananeelcinta.util.UserConfiguration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity(), ImageSliderAdapter.OnItemClickListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var adapter: SimpleRecyclerAdapter<ModuleView>
    private val moduleList = arrayListOf<ModuleView>()

    private lateinit var weeklyReflectionFragment: DetailWeeklyReflectionFragment

    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference
    val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    val pastorMessagesReference: DatabaseReference = databaseReference.child("pastorMessages")
    val announcementReference: DatabaseReference = databaseReference.child("announcement")

    private lateinit var pastorMessage: PastorMessage
    private var announcementList: ArrayList<Announcement> = arrayListOf()

    private lateinit var viewPager: ViewPager2
    private lateinit var imageSliderAdapter: ImageSliderAdapter

    private val isAdmin =
        UserConfiguration.getInstance().getUserData()?.role.equals(Role.SUPERUSER.role)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        supportActionBar?.hide()

        setupLayout()
        getAnnouncements()
    }

    override fun onResume() {
        super.onResume()
        getProfileImage()
    }

    private fun setupLayout() {
        setupModuleView()
        setupMenuRecyclerView()

        binding.ivProfile.setOnClickListener {
            val intent = Intent(applicationContext, ProfileActivity::class.java)
            startActivity(intent)
        }
        binding.tvUsername.text = UserConfiguration.getInstance().getUserData()!!.fullName + "!"
        getProfileImage()
        getBirthdayCard()
        getPastorMessages()
        setupImageSlider()
    }

    private fun setupMenuRecyclerView() {
        binding.rvMenu.layoutManager = GridLayoutManager(applicationContext, 3)
        adapter = SimpleRecyclerAdapter<ModuleView>(
            moduleList,
            R.layout.item_menu
        ) { holder, item ->
            val itemBinding: ItemMenuBinding = holder.layoutBinding as ItemMenuBinding
            itemBinding.icMenu.setImageDrawable(resources.getDrawable(item.moduleIcon))
            itemBinding.tvMenu.text = item.moduleName
            itemBinding.root.setOnClickListener {
                val intent: Intent = when (item.moduleName) {
                    resources.getString(R.string.services) ->
                        Intent(applicationContext, ServicesActivity::class.java)

                    resources.getString(R.string.green_room) ->
                        Intent(applicationContext, GreenRoomActivity::class.java)

                    resources.getString(R.string.reflection) ->
                        Intent(applicationContext, PastorMessagesListActivity::class.java)

                    resources.getString(R.string.offering) ->
                        Intent(applicationContext, OfferingActivity::class.java)

                    resources.getString(R.string.prayer_request) ->
                        Intent(applicationContext, PrayerRequestListActivity::class.java)


                    else -> Intent(applicationContext, MainActivity::class.java)
                }
                startActivity(intent)
            }
        }
        binding.rvMenu.adapter = adapter
        binding.rvMenu.isNestedScrollingEnabled = false
    }

    private fun setupModuleView() {
        val servicesModule =
            ModuleView(resources.getString(R.string.services), R.drawable.ic_church)
        val greenRoomModule =
            ModuleView(resources.getString(R.string.green_room), R.drawable.meeting)
        val eventModule =
            ModuleView(resources.getString(R.string.reflection), R.drawable.ic_light)
        val offeringModule =
            ModuleView(resources.getString(R.string.offering), R.drawable.ic_give)
        val prayerRequestModule =
            ModuleView(resources.getString(R.string.prayer_request), R.drawable.ic_pray)

        moduleList.add(servicesModule)
        moduleList.add(prayerRequestModule)
        moduleList.add(eventModule)
        moduleList.add(offeringModule)

        //admin menu
        if (UserConfiguration.getInstance().getUserData()?.role != null
            && isAdmin
        ) {
            moduleList.add(greenRoomModule)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    private fun getProfileImage() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { currentUser ->
            val profilePicturesRef = storageRef.child("${currentUser.uid}/profile-pictures")
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
                        .apply(requestOptions)
                        .into(binding.ivProfile)
                }
            }
        }
    }

    private fun getBirthdayCard() {
        val birthdayCard: MaterialCardView = findViewById(R.id.birthday_card)
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { currentUser ->
            databaseReference.child("users/${currentUser.uid}/dateOfBirth").get()
                .addOnSuccessListener {
                    val dateOfBirthFormat =
                        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).parse(it.value.toString())
                    val dateFormat = SimpleDateFormat("dd-MM", Locale.getDefault())
                    val dateOfBirthFormatted =
                        dateOfBirthFormat?.let { df -> dateFormat.format(df) }
                    val todayDateFormatted = dateFormat.format(Date())
                    if (dateOfBirthFormatted == "05-12") {
                        birthdayCard.visibility = View.VISIBLE
                    } else {
                        birthdayCard.visibility = View.GONE
                    }
                }.addOnFailureListener {
                Log.e("MainActivity", "Failed to retrieve DOB.")
            }
        }
    }

    private fun getPastorMessages() {
        val query: Query = pastorMessagesReference.limitToLast(1)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (messagesSnapshot in snapshot.children) {
                        pastorMessage = messagesSnapshot.getValue(PastorMessage::class.java)!!
                        binding.pastorMessageTitle.text = pastorMessage.title
                        binding.pastorMessageText.text = pastorMessage.messages
                        binding.btnWeeklyReflectionDetail.setOnClickListener {
                            weeklyReflectionFragment = DetailWeeklyReflectionFragment.newInstance()
                            val bundle = Bundle().apply {
                                putParcelable(
                                    DetailWeeklyReflectionFragment.DETAIL_MESSAGE,
                                    pastorMessage
                                )
                            }
                            weeklyReflectionFragment.arguments = bundle
                            weeklyReflectionFragment.show(
                                supportFragmentManager,
                                "weekly_reflection_detail"
                            )
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getAnnouncements() {
        val query: Query = announcementReference.limitToLast(5)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val tempList: ArrayList<Announcement> = arrayListOf()
                    for (announcementSnapshot in snapshot.children) {
                        val model = announcementSnapshot.getValue(Announcement::class.java)
                        if (model != null) {
                            tempList.add(model)
                        }
                    }
                    announcementList = tempList
                    viewPager = binding.slider
                    imageSliderAdapter =
                        ImageSliderAdapter(announcementList.sortedByDescending { it.date })
                    imageSliderAdapter.setOnItemClickListener(this@MainActivity)
                    viewPager.adapter = imageSliderAdapter
                    startAutoScroll()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private val autoScrollHandler = Handler()
    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            val currentItem = viewPager.currentItem
            val nextItem = (currentItem + 1) % announcementList.size
            viewPager.setCurrentItem(nextItem, true)
            autoScrollHandler.postDelayed(this, 8000)
        }
    }

    private fun setupImageSlider() {

    }

    private fun startAutoScroll() {
        autoScrollHandler.postDelayed(autoScrollRunnable, 8000) // Auto-scroll every 3 seconds
    }

    override fun onDestroy() {
        super.onDestroy()
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
    }

    override fun onItemClick(announcement: Announcement) {
        val intent = Intent(this, DetailAnnouncementActivity::class.java)
        intent.putExtra("announcement", announcement)
        startActivity(intent)
    }
}