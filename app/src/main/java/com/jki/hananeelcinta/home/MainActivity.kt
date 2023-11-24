package com.jki.hananeelcinta.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityMainBinding
import com.jki.hananeelcinta.databinding.ItemMenuBinding
import com.jki.hananeelcinta.home.weeklyreflection.DetailWeeklyReflectionFragment
import com.jki.hananeelcinta.model.Announcement
import com.jki.hananeelcinta.model.PastorMessage
import com.jki.hananeelcinta.pastoral.GreenRoomActivity
import com.jki.hananeelcinta.pastoral.announcement.DetailAnnouncementActivity
import com.jki.hananeelcinta.prayerrequest.CreatePrayerRequestActivity
import com.jki.hananeelcinta.prayerrequest.PrayerRequestListActivity
import com.jki.hananeelcinta.profile.ProfileActivity
import com.jki.hananeelcinta.reflection.ReflectionActivity
import com.jki.hananeelcinta.services.ServicesActivity
import com.jki.hananeelcinta.util.ImageSliderAdapter
import com.jki.hananeelcinta.util.SimpleRecyclerAdapter
import com.jki.hananeelcinta.util.UserConfiguration


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        supportActionBar?.hide()

        setupLayout()
        getAnnouncements()
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

                    resources.getString(R.string.prayer_request) ->
                        Intent(applicationContext, CreatePrayerRequestActivity::class.java)

                    resources.getString(R.string.pray_request_list) ->
                        Intent(applicationContext, PrayerRequestListActivity::class.java)

                    else -> Intent(applicationContext, ReflectionActivity::class.java)
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
        val prayerRequestModule =
            ModuleView(resources.getString(R.string.prayer_request), R.drawable.ic_pray)
        val prayRequestListModule =
            ModuleView(resources.getString(R.string.pray_request_list), R.drawable.ic_pray)

        moduleList.add(servicesModule)
        moduleList.add(prayerRequestModule)
        moduleList.add(eventModule)

        //admin menu
        if (UserConfiguration.getInstance().getUserData()?.role != null
            && UserConfiguration.getInstance().getUserData()?.role.equals("admin")
        ) {
            moduleList.add(greenRoomModule)
            moduleList.add(prayRequestListModule)
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
                    for (announcementSnapshot in snapshot.children) {
                        val model = announcementSnapshot.getValue(Announcement::class.java)
                        if (model != null) {
                            announcementList.add(model)
                        }
                    }
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