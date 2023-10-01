package com.jki.hananeelcinta.pastoral.pastormessages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityPastorMessagesListBinding
import com.jki.hananeelcinta.databinding.ItemPastorMessageBinding
import com.jki.hananeelcinta.home.weeklyreflection.DetailWeeklyReflectionFragment
import com.jki.hananeelcinta.model.PastorMessage
import com.jki.hananeelcinta.util.SimpleRecyclerAdapter
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class PastorMessagesListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPastorMessagesListBinding

    private var pastorMessageList: ArrayList<PastorMessage> = arrayListOf()
    private lateinit var adapter: SimpleRecyclerAdapter<PastorMessage>

    private val database = FirebaseDatabase.getInstance()
    private val pastorMessageRef = database.getReference("pastorMessages")

    private lateinit var weeklyReflectionFragment: DetailWeeklyReflectionFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pastor_messages_list)
        supportActionBar?.hide()
        setupLayout()
        getPastorMessagesList()
    }

    private fun setupLayout() {
        binding.fabCreatePastorMessage.setOnClickListener {
            val intent = Intent(this, CreatePastorMessagesActivity::class.java)
            startActivity(intent)
        }
        binding.btnBack.setOnClickListener { onBackPressed() }
        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.rvPastorMessage.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter = SimpleRecyclerAdapter(
            arrayListOf(),
            R.layout.item_pastor_message
        ) { holder, item ->
            val itemBinding: ItemPastorMessageBinding =
                holder?.layoutBinding as ItemPastorMessageBinding

            itemBinding.tvDate.text = getReadableDate(item.date)
            itemBinding.tvTitle.text = item.title
            itemBinding.tvValue.text = item.messages
            "Written by:  ${item.writer}".also { itemBinding.tvWriter.text = it }

            itemBinding.root.setOnClickListener {
                weeklyReflectionFragment = DetailWeeklyReflectionFragment.newInstance()
                val bundle = Bundle().apply {
                    putParcelable(
                        DetailWeeklyReflectionFragment.DETAIL_MESSAGE,
                        item
                    )
                }
                weeklyReflectionFragment.arguments = bundle
                weeklyReflectionFragment.show(
                    supportFragmentManager,
                    "weekly_reflection_detail"
                )
            }
        }
        binding.rvPastorMessage.adapter = adapter
    }

    private fun getReadableDate(timeMilis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
        val currentDate = Date(timeMilis)
        return sdf.format(currentDate)
    }

    private fun getPastorMessagesList() {
        pastorMessageRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val pm = dataSnapshot.getValue(PastorMessage::class.java)
                        if (pm != null) {
                            pastorMessageList.add(pm)
                        }
                    }
                    adapter.mainData = pastorMessageList.sortedByDescending { it.date }
                    binding.isError = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.isError = true
            }

        })
    }

}