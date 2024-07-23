package com.jki.hananeelcinta.prayerrequest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityPrayerRequestListBinding
import com.jki.hananeelcinta.databinding.ItemAnnouncementBinding
import com.jki.hananeelcinta.databinding.ItemPrayRequestBinding
import com.jki.hananeelcinta.model.Announcement
import com.jki.hananeelcinta.model.PrayerRequest
import com.jki.hananeelcinta.model.Role
import com.jki.hananeelcinta.model.Status
import com.jki.hananeelcinta.util.SimpleFilterRecyclerAdapter
import com.jki.hananeelcinta.util.UserConfiguration
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class PrayerRequestListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrayerRequestListBinding
    private lateinit var adapter: SimpleFilterRecyclerAdapter<PrayerRequest>

    private val database = FirebaseDatabase.getInstance()
    private val databaseReference = database.getReference("prayerRequest")

    private var prayRequestList: ArrayList<PrayerRequest> = arrayListOf()

    private var isAdmin =
        UserConfiguration.getInstance().getUserData()?.role.equals(Role.SUPERUSER.role)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_prayer_request_list)
        supportActionBar?.hide()

        setupLayout()
        getPrayRequestList()
    }

    private fun setupLayout() {
        binding.btnBack.setOnClickListener { onBackPressed() }
        initRecyclerView()
        setupCreateButton()
    }

    private fun setupCreateButton() {
        binding.fabCreatePrayRequest.setOnClickListener {
            val intent = Intent(applicationContext, CreatePrayerRequestActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initRecyclerView() {
        binding.rvPrayRequest.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter = SimpleFilterRecyclerAdapter(
            arrayListOf(),
            R.layout.item_pray_request,
            { holder, item ->
                val itemBinding: ItemPrayRequestBinding =
                    holder?.layoutBinding as ItemPrayRequestBinding

                itemBinding.tvPrayRequestType.text = item?.prayType
                itemBinding.tvPrayRequester.text = item?.requesterName
                itemBinding.tvPrayRequestDate.text =
                    item.id?.let { convertTimestampToReadableDate(it.toLong()) }

                when (item?.status) {
                    Status.OPEN.name -> {
                        itemBinding.cardStatus.setCardBackgroundColor(resources.getColor(R.color.greyAAAAAA))
                        itemBinding.tvStatus.text = Status.OPEN.status
                    }

                    Status.IN_PROGRESS.name -> {
                        itemBinding.cardStatus.setCardBackgroundColor(resources.getColor(R.color.yellowC79C52))
                        itemBinding.tvStatus.text = Status.IN_PROGRESS.status
                    }

                    Status.DONE.name -> {
                        itemBinding.cardStatus.setCardBackgroundColor(resources.getColor(R.color.green40BB74))
                        itemBinding.tvStatus.text = Status.DONE.status
                    }

                    Status.CANCELLED.name -> {
                        itemBinding.cardStatus.setCardBackgroundColor(resources.getColor(R.color.redD11008))
                        itemBinding.tvStatus.text = Status.CANCELLED.status
                    }
                }

                itemBinding.root.setOnClickListener {
                    val intent = Intent(applicationContext, PrayerRequestDetailActivity::class.java)
                    intent.putExtra("prayRequest", item)
                    startActivity(intent)
                }
            }, object : SimpleFilterRecyclerAdapter.OnSearchListener<PrayerRequest> {
                override fun onSearchRules(
                    model: PrayerRequest?,
                    searchedText: String?
                ): PrayerRequest? {
                    if (searchedText?.let {
                            model?.prayType.toString().lowercase(Locale.getDefault())
                                .contains(it)
                        } == true || searchedText?.let {
                            model?.requesterName.toString().lowercase(Locale.getDefault())
                                .contains(it)
                        } == true) {
                        showPrayRequestNotFound(false)
                        return model!!
                    }
                    return null
                }

                override fun onSearch(model: ArrayList<PrayerRequest>?) {
                }

                override fun onSearchEmpty(searchedText: String?) {
                    showPrayRequestNotFound(true)
                }

            }
        )
        binding.rvPrayRequest.adapter = adapter
    }

    private fun convertTimestampToReadableDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }

    private fun showPrayRequestNotFound(show: Boolean) {
        binding.lyNotFound.tvTitle.text = resources.getString(R.string.pray_request_not_found_title)
        binding.lyNotFound.tvInfo.text = resources.getString(R.string.pray_request_not_found_desc)
        binding.isPrayRequestNotFound = show
    }

    private fun getPrayRequestList() {
        if (isAdmin) {
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val tempList: ArrayList<PrayerRequest> = arrayListOf()
                        for (dataSnapshot in snapshot.children) {
                            var prayRequest = dataSnapshot.getValue(PrayerRequest::class.java)
                            if (prayRequest != null) {
                                tempList.add(prayRequest)
                            }
                        }
                        prayRequestList = tempList
                        adapter.mainData = prayRequestList.sortedByDescending { it.id }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.isError = true
                }

            })
        } else {
            val userId = UserConfiguration.getInstance().getUserId()
            val query: Query = databaseReference.orderByChild("requesterId").equalTo(userId)
            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val tempList: ArrayList<PrayerRequest> = arrayListOf()
                    for (snaphost in dataSnapshot.children) {
                        val prayRequest = snaphost.getValue(PrayerRequest::class.java)
                        if (prayRequest != null) {
                            tempList.add(prayRequest)
                        }
                    }
                    prayRequestList = tempList
                    adapter.mainData = prayRequestList.sortedByDescending { it.id }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.isError = true
                }

            })
        }
    }
}