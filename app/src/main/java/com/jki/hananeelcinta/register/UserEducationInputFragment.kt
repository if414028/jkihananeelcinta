package com.jki.hananeelcinta.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.FragmentUserEducationInformationInputBinding
import com.jki.hananeelcinta.model.BloodType
import com.jki.hananeelcinta.model.Education
import com.jki.hananeelcinta.model.Job

class UserEducationInputFragment : Fragment() {

    private lateinit var binding: FragmentUserEducationInformationInputBinding
    private lateinit var viewModel: RegisterViewModel

    companion object {
        @JvmStatic
        fun newInstance() = UserEducationInputFragment().apply { }
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
            R.layout.fragment_user_education_information_input,
            container,
            false
        )

        setupBloodTypeRadioButton()
        setupLastEducationRadioButton()
        setupJobRadioButton()

        return binding.root
    }

    private fun setupBloodTypeRadioButton() {
        enumValues<BloodType>().forEach {
            val rbBloodType = RadioButton(context)
            rbBloodType.text = it.bloodType
            binding.rbBloodType.addView(rbBloodType)
        }
        (binding.rbBloodType.getChildAt(0) as RadioButton).isChecked = true
    }

    private fun getSelectedBloodType(): String {
        val checkedRadioButton =
            binding.root.findViewById<RadioButton>(binding.rbBloodType.checkedRadioButtonId)
        return checkedRadioButton.text.toString()
    }

    private fun setupLastEducationRadioButton() {
        enumValues<Education>().forEach {
            val rbEducation = RadioButton(context)
            rbEducation.text = it.education
            binding.rbLastEducation.addView(rbEducation)
        }
        (binding.rbLastEducation.getChildAt(0) as RadioButton).isChecked = true
    }

    private fun getSelectedEducation(): String {
        val checkRadioButton =
            binding.root.findViewById<RadioButton>(binding.rbLastEducation.checkedRadioButtonId)

        if (checkRadioButton.text.toString().equals("Lainnya")) {
            return checkRadioButton.text.toString() + " - " + binding.etLastEducation.text
        }

        return checkRadioButton.text.toString()
    }

    private fun setupJobRadioButton() {
        enumValues<Job>().forEach {
            val rbJob = RadioButton(context)
            rbJob.text = it.job
            binding.rbWork.addView(rbJob)
        }
        (binding.rbWork.getChildAt(0) as RadioButton).isChecked = true
    }

    private fun getSelectedJob(): String {
        val checkedRadioButton =
            binding.root.findViewById<RadioButton>(binding.rbWork.checkedRadioButtonId)

        if (checkedRadioButton.text.toString().equals("Lainnya")) {
            return checkedRadioButton.text.toString() + " - " + binding.etWork.text
        }

        return checkedRadioButton.text.toString()
    }

    fun setUserEducation() {
        viewModel.setUserEducation(
            getSelectedBloodType(),
            getSelectedEducation(),
            getSelectedJob(),
        )
    }
}