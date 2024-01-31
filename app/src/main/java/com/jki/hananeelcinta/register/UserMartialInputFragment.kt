package com.jki.hananeelcinta.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.FragmentUserMartialInformationInputBinding
import com.jki.hananeelcinta.model.FamilyStatus
import com.jki.hananeelcinta.model.HasMarriedQuestion

class UserMartialInputFragment : Fragment() {

    private lateinit var binding: FragmentUserMartialInformationInputBinding
    private lateinit var viewModel: RegisterViewModel

    companion object {
        @JvmStatic
        fun newInstance() = UserMartialInputFragment().apply { }
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
            R.layout.fragment_user_martial_information_input,
            container,
            false
        )

        setupMarriedStatusRadioButton()
        setupFamilyStatusRadioButton()

        return binding.root
    }

    private fun setupMarriedStatusRadioButton() {
        enumValues<HasMarriedQuestion>().forEach {
            val rbHasMarried = RadioButton(context)
            rbHasMarried.text = it.hasMarriedQuestion
            binding.rbMartialStatus.addView(rbHasMarried)
        }
        (binding.rbMartialStatus.getChildAt(0) as RadioButton).isChecked = true
    }

    private fun getMarriedStatusInformation(): String {
        val checkRadioButton =
            binding.root.findViewById<RadioButton>(binding.rbMartialStatus.checkedRadioButtonId)
        return checkRadioButton.text.toString()
    }

    private fun setupFamilyStatusRadioButton() {
        enumValues<FamilyStatus>().forEach {
            val rbFamilyStatus = RadioButton(context)
            rbFamilyStatus.text = it.familyStatus
            binding.rbStatusInFamily.addView(rbFamilyStatus)
        }
        (binding.rbStatusInFamily.getChildAt(0) as RadioButton).isChecked = true
    }

    private fun getFamilyStatusInformation(): String {
        val checkRadioButton =
            binding.root.findViewById<RadioButton>(binding.rbStatusInFamily.checkedRadioButtonId)
        return checkRadioButton.text.toString()
    }

    fun setMartialStatusInformation() {
        viewModel.setUserMartialStatus(
            getMarriedStatusInformation(),
            getFamilyStatusInformation()
        )
    }

    fun validateSection() {
        viewModel.setIsSectionValid(
            true
        )
    }
}