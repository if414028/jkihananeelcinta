package com.jki.hananeelcinta.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.FragmentUserFamilyMemberInputBinding
import com.jki.hananeelcinta.model.FamilyStatus
import com.jki.hananeelcinta.model.PastorMessage
import com.jki.hananeelcinta.util.SimpleRecyclerAdapter

class UserFamilyMemberInputFragment : Fragment() {

    private lateinit var binding: FragmentUserFamilyMemberInputBinding
    private lateinit var viewModel: RegisterViewModel

    private lateinit var familyStatus: String

    private var childrenList: ArrayList<String> = arrayListOf()
    private lateinit var childrenAdapter: SimpleRecyclerAdapter<String>

    private var siblingsList: ArrayList<String> = arrayListOf()
    private lateinit var siblingsAdapter: SimpleRecyclerAdapter<String>

    companion object {
        @JvmStatic
        fun newInstance() = UserFamilyMemberInputFragment().apply { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.let { ViewModelProvider(it)[RegisterViewModel::class.java] }!!
        familyStatus = viewModel.user.statusInFamily
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_user_family_member_input,
            container,
            false
        )

        initLayout()

        return binding.root
    }

    private fun setupChildrenRecyclerView() {

    }

    private fun initLayout() {
        when (familyStatus) {
            FamilyStatus.HEAD_OF_FAMILY.name -> {
                binding.lyWife.visibility = View.VISIBLE
                binding.rvChildren.visibility = View.VISIBLE
            }

            FamilyStatus.WIFE.name -> {
                binding.lyHusbandName.visibility = View.VISIBLE
                binding.rvChildren.visibility = View.VISIBLE
            }

            FamilyStatus.CHILD.name -> {
                binding.rvSiblings.visibility = View.VISIBLE
            }

            FamilyStatus.WIDOW.name -> {
                binding.rvChildren.visibility = View.VISIBLE
            }

            FamilyStatus.WIDOWER.name -> {
                binding.rvChildren.visibility = View.VISIBLE
            }
        }
    }
}