package com.jki.hananeelcinta.register

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.FragmentUserFamilyMemberInputBinding

class UserFamilyMemberFragment : Fragment() {
    private lateinit var binding: FragmentUserFamilyMemberInputBinding
    private lateinit var viewModel: RegisterViewModel

    private lateinit var database: DatabaseReference
    private lateinit var userList: MutableList<String>
    private lateinit var adapter: ArrayAdapter<String>

    companion object {
        @JvmStatic
        fun newInstance() = UserFamilyMemberFragment().apply { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.let { ViewModelProvider(it).get(RegisterViewModel::class.java) }!!
        database = FirebaseDatabase.getInstance().reference.child("users")
        userList = mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_user_family_member_input,
            container,
            false
        )

        setupHeadOfFamilySection()

        return binding.root
    }

    private fun setupHeadOfFamilySection() {
        adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, userList)
        binding.autoCompleteTextView.setAdapter(adapter)

        binding.autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.isNotEmpty()) {
                    searchUsers(s.toString())
                } else {
                    userList.clear()
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    private fun searchUsers(query: String) {
        val queryRef = database.orderByChild("fullName").startAt(query).endAt(query + "\uf8ff")

        queryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (userSnapshot in snapshot.children) {
                    val userName = userSnapshot.child("fullName").getValue(String::class.java)
                    if (userName != null) {
                        userList.add(userName)
                    }
                }
                adapter.notifyDataSetChanged()
                if (userList.isNotEmpty()) {
                    binding.autoCompleteTextView.showDropDown()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    fun setHeadOfFamilyId() {
        viewModel.setHeadOfFamilyId(binding.autoCompleteTextView.text.toString())
    }
}