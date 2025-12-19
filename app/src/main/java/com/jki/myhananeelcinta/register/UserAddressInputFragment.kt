package com.jki.myhananeelcinta.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jki.myhananeelcinta.R
import com.jki.myhananeelcinta.databinding.FragmentUserAddressInputBinding

class UserAddressInputFragment : Fragment() {

    private lateinit var binding: FragmentUserAddressInputBinding
    private lateinit var viewModel: RegisterViewModel

    companion object {
        @JvmStatic
        fun newInstance() = UserAddressInputFragment().apply { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.let { ViewModelProvider(it).get(RegisterViewModel::class.java) }!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_user_address_input,
            container,
            false
        )

        validateEachField()

        return binding.root
    }

    fun setUserAddress() {
        viewModel.setUserAddress(binding.etAddress.text.toString())
    }

    private fun validateEachField() {
        var isAddressValid = false
        binding.etAddress.addTextChangedListener {
            isAddressValid = it.toString().isNotEmpty()
            if (isAddressValid)
                binding.tvAddressErrorMessage.visibility = View.GONE
            else binding.tvAddressErrorMessage.visibility = View.VISIBLE
            viewModel.setIsSectionValid(isAddressValid)
        }
    }

    fun validateSection() {
        viewModel.setIsSectionValid(binding.etAddress.text.toString().isNotEmpty())
    }
}