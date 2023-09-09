package com.jki.hananeelcinta.pastoral.pastormessages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityPastorMessagesListBinding
import com.jki.hananeelcinta.model.PastorMessage
import com.jki.hananeelcinta.util.SimpleRecyclerAdapter

class PastorMessagesListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPastorMessagesListBinding

    private var pastorMessageList: List<PastorMessage> = arrayListOf()
    private lateinit var adapter: SimpleRecyclerAdapter<PastorMessage>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pastor_messages_list)
        setupLayout()
    }

    private fun setupLayout() {
        binding.fabCreatePastorMessage.setOnClickListener {
            val intent = Intent(this, CreatePastorMessagesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initRecyclerView() {

    }
}