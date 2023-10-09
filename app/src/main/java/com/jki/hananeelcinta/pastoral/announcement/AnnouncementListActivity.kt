package com.jki.hananeelcinta.pastoral.announcement

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
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
import com.jki.hananeelcinta.databinding.ActivityAnnouncementListBinding
import com.jki.hananeelcinta.databinding.ItemAnnouncementBinding
import com.jki.hananeelcinta.model.Announcement
import com.jki.hananeelcinta.model.User
import com.jki.hananeelcinta.pastoral.pastormessages.CreatePastorMessagesActivity
import com.jki.hananeelcinta.util.SimpleFilterRecyclerAdapter
import com.jki.hananeelcinta.util.SimpleRecyclerAdapter
import com.otaliastudios.cameraview.filter.SimpleFilter
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class AnnouncementListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnnouncementListBinding

    private val database = FirebaseDatabase.getInstance()
    private val announcementRef = database.getReference("announcement")
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference

    private lateinit var adapter: SimpleFilterRecyclerAdapter<Announcement>
    private var announcementList: ArrayList<Announcement> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_announcement_list)
        supportActionBar?.hide()

        setupLayout()
        getAnnouncementList()
    }

    private fun setupLayout() {
        binding.btnBack.setOnClickListener { onBackPressed() }
        initRecyclerView()
        binding.fabCreateAnnouncement.setOnClickListener {
            val intent = Intent(this, CreateAnnouncementActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initRecyclerView() {
        binding.rvAnnouncement.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter = SimpleFilterRecyclerAdapter(
            arrayListOf(),
            R.layout.item_announcement,
            { holder, item ->
                val itemBinding: ItemAnnouncementBinding =
                    holder?.layoutBinding as ItemAnnouncementBinding

                itemBinding.tvTitle.text = item?.title
                itemBinding.tvSubtitle.text = item?.desc
                itemBinding.tvDate.text = item?.date?.let { convertTimestampToReadableDate(it) }
                if (item?.imageUrl != null) {
                    showAnnouncementImage(item.imageUrl, itemBinding.ivAnnouncement)
                }
            }, object : SimpleFilterRecyclerAdapter.OnSearchListener<Announcement> {
                override fun onSearchRules(
                    model: Announcement?,
                    searchedText: String?
                ): Announcement? {
                    if (searchedText?.let {
                            model?.title.toString().lowercase(Locale.getDefault())
                                .contains(it)
                        } == true || searchedText?.let {
                            model?.desc.toString().lowercase(Locale.getDefault())
                                .contains(it)
                        } == true) {
                        showAnnouncementNotFoundLayout(false)
                        return model!!
                    }
                    return null
                }

                override fun onSearch(model: ArrayList<Announcement>?) {
                }

                override fun onSearchEmpty(searchedText: String?) {
                    showAnnouncementNotFoundLayout(true)
                }

            }
        )
        binding.rvAnnouncement.adapter = adapter
    }

    private fun showAnnouncementNotFoundLayout(show: Boolean) {
        binding.lyNotFound.tvTitle.text = resources.getString(R.string.announcement_not_found_title)
        binding.lyNotFound.tvInfo.text = resources.getString(R.string.announcement_not_found_info)
        binding.isAnnouncementNotFound = show
    }

    private fun convertTimestampToReadableDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }

    private fun showAnnouncementImage(imageUrl: String, ivAnnouncement: ImageView) {
        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both the original and resized image

        Glide.with(applicationContext)
            .load(imageUrl)
            .placeholder(R.drawable.ic_no_profile_image)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .apply(requestOptions)
            .into(ivAnnouncement)
    }

    private fun getAnnouncementList() {
        announcementRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        var announcement = dataSnapshot.getValue(Announcement::class.java)
                        if (announcement != null) {
                            announcementList.add(announcement)
                        }
                    }
                    adapter.mainData = announcementList.sortedByDescending { it.date }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}