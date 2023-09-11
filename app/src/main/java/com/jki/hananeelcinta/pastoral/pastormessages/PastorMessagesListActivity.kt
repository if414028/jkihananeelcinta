package com.jki.hananeelcinta.pastoral.pastormessages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityPastorMessagesListBinding
import com.jki.hananeelcinta.databinding.ItemPastorMessageBinding
import com.jki.hananeelcinta.model.PastorMessage
import com.jki.hananeelcinta.util.SimpleRecyclerAdapter

class PastorMessagesListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPastorMessagesListBinding

    private var pastorMessageList: List<PastorMessage> = arrayListOf()
    private lateinit var adapter: SimpleRecyclerAdapter<PastorMessage>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pastor_messages_list)
        supportActionBar?.hide()
        setupLayout()
    }

    private fun setupLayout() {
        binding.fabCreatePastorMessage.setOnClickListener {
            val intent = Intent(this, CreatePastorMessagesActivity::class.java)
            startActivity(intent)
        }

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
        }
    }
}