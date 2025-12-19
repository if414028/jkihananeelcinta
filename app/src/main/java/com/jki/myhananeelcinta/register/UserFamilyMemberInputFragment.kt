package com.jki.myhananeelcinta.register

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jki.myhananeelcinta.R
import com.jki.myhananeelcinta.databinding.FragmentUserFamilyMemberInputBinding
import com.jki.myhananeelcinta.databinding.ItemNameBinding
import com.jki.myhananeelcinta.model.FamilyStatus
import com.jki.myhananeelcinta.util.SimpleRecyclerAdapter

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
        binding.rvChildren.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        childrenAdapter =
            SimpleRecyclerAdapter(arrayListOf(), R.layout.item_name) { holder, item ->
                val itemBinding: ItemNameBinding = holder?.layoutBinding as ItemNameBinding
                itemBinding.etName.hint =
                    requireActivity().resources.getString(R.string.siblings_name)
                itemBinding.etName.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun afterTextChanged(text: Editable?) {
                        if (text != null) {
                            childrenList[holder.layoutPosition] = text.toString()
                        }
                    }

                })
            }
        childrenList.add("")
        binding.rvChildren.adapter = childrenAdapter
        childrenAdapter.mainData = childrenList

        binding.btnAddChild.setOnClickListener {
            if (childrenAdapter.mainData.size < 11) {
                addChildrenField()
            }
        }
    }

    private fun addChildrenField() {
        childrenList.add("")
        childrenAdapter.notifyItemInserted(childrenList.size + 1)
    }

    private fun setupSiblingsRecyclerView() {
        binding.rvSiblings.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        siblingsAdapter =
            SimpleRecyclerAdapter(arrayListOf(), R.layout.item_name) { holder, _ ->
                val itemBinding: ItemNameBinding = holder?.layoutBinding as ItemNameBinding
                itemBinding.etName.hint =
                    requireActivity().resources.getString(R.string.siblings_name)
                itemBinding.etName.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun afterTextChanged(text: Editable?) {
                        if (text != null) {
                            siblingsList[holder.layoutPosition] = text.toString()
                        }
                    }

                })
            }
        siblingsList.add("")
        binding.rvSiblings.adapter = siblingsAdapter
        siblingsAdapter.mainData = siblingsList

        binding.btnAddSibling.setOnClickListener {
            addSiblingsField()
        }
    }

    private fun addSiblingsField() {
        siblingsList.add("")
        siblingsAdapter.notifyItemInserted(siblingsList.size + 1)
    }

    fun initLayout() {
        when (viewModel.user.statusInFamily) {
            FamilyStatus.HEAD_OF_FAMILY.familyStatus -> {
                binding.lyWife.visibility = View.VISIBLE
                binding.lyChildren.visibility = View.VISIBLE
                setupChildrenRecyclerView()
            }

            FamilyStatus.WIFE.familyStatus -> {
                binding.lyHusband.visibility = View.VISIBLE
                binding.lyChildren.visibility = View.VISIBLE
                setupChildrenRecyclerView()
            }

            FamilyStatus.CHILD.familyStatus -> {
                binding.lySiblings.visibility = View.VISIBLE
                setupSiblingsRecyclerView()
            }

            FamilyStatus.WIDOW.familyStatus -> {
                binding.lyChildren.visibility = View.VISIBLE
                setupChildrenRecyclerView()
            }

            FamilyStatus.WIDOWER.familyStatus -> {
                binding.lyChildren.visibility = View.VISIBLE
                setupChildrenRecyclerView()
            }
        }
    }

    private fun getChildrenName(): String {
        val childrenName = StringBuilder()
        for (name in childrenList) {
            if (name.isNotEmpty() && name.isNotBlank()) {
                childrenName.append(name)
                childrenName.append(",")
            }
        }
        if (childrenName.isNotEmpty() && childrenName.isNotBlank()) {
            childrenName.deleteCharAt(childrenName.length - 1)
        }

        return childrenName.toString()
    }

    private fun getSiblingsName(): String {
        val siblingsName = StringBuilder()
        for (name in siblingsList) {
            if (name.isNotEmpty() && name.isNotBlank()) {
                siblingsName.append(name)
                siblingsName.append(",")
            }
        }
        if (siblingsName.isNotEmpty() && siblingsName.isNotBlank()) {
            siblingsName.deleteCharAt(siblingsName.length - 1)
        }

        return siblingsName.toString()
    }

    fun setFamilyMemberData() {
        viewModel.setFamilyMemberData(
            binding.etWifeName.text.toString(),
            binding.etHusbandName.text.toString(),
            getChildrenName(),
            getSiblingsName()
        )
    }
}