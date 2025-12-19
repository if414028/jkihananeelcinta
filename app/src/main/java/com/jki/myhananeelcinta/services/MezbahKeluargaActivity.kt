package com.jki.myhananeelcinta.services

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jki.myhananeelcinta.R
import com.jki.myhananeelcinta.databinding.ActivityMezbahKeluargaBinding
import com.jki.myhananeelcinta.databinding.ItemMezbahKeluargaBinding
import com.jki.myhananeelcinta.model.Mk
import com.jki.myhananeelcinta.util.SimpleRecyclerAdapter

class MezbahKeluargaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMezbahKeluargaBinding

    private val database = FirebaseDatabase.getInstance()
    private val mkRef = database.getReference("mk")
    private var mkData: ArrayList<Mk> = arrayListOf()
    private lateinit var adapter: SimpleRecyclerAdapter<Mk>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_mezbah_keluarga)
        supportActionBar?.hide()
        getMkData()
        setupLayout()
    }

    private fun setupLayout() {
        binding.btnBack.setOnClickListener { onBackPressed() }
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvMezbahKeluarga.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter = SimpleRecyclerAdapter(
            mkData, R.layout.item_mezbah_keluarga
        ) { holder, item ->
            val itemBinding: ItemMezbahKeluargaBinding =
                holder?.layoutBinding as ItemMezbahKeluargaBinding
            itemBinding.tvDay.text = item?.day
            itemBinding.tvTime.text = item?.time
            itemBinding.tvLocation.text = item?.location
            itemBinding.tvPic.text = item?.pic
            itemBinding.tvContact.text = item?.contact

            if (item.desc.isNotEmpty() && item.desc != "-") {
                itemBinding.tvDesc.visibility = View.VISIBLE
                itemBinding.tvDesc.text = item?.desc
            }

            itemBinding.btnWhatsapp.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                val message = getString(R.string.mk_message, item.pic)
                intent.data =
                    Uri.parse("https://api.whatsapp.com/send?phone=${item.contact}&text=$message")
                startActivity(intent)
            }
        }
        binding.rvMezbahKeluarga.adapter = adapter
    }

    private fun getMkData() {
        mkRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.lyError.visibility = View.GONE
                    for (dataSnapshot in snapshot.children) {
                        val mk = dataSnapshot.getValue(Mk::class.java)
                        if (mk != null) {
                            mkData.add(mk)
                        }
                    }
                    adapter.mainData = mkData
                } else {
                    binding.lyError.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.lyError.visibility = View.VISIBLE
            }

        })
    }
}