package com.jki.hananeelcinta.register

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.RadioButton
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.FragmentUserInformationInputBinding
import com.jki.hananeelcinta.model.Gender
import java.text.SimpleDateFormat
import java.util.*

class UserInformationInputFragment : Fragment() {

    private lateinit var binding: FragmentUserInformationInputBinding
    private lateinit var viewModel: RegisterViewModel

    private lateinit var calendar: Calendar

    companion object {
        @JvmStatic
        fun newInstance() = UserInformationInputFragment().apply { }
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
            R.layout.fragment_user_information_input,
            container,
            false
        )
        setupGenderRadioButton()
        setupDatePicker()

        validateEachField()

        return binding.root
    }

    private fun setupGenderRadioButton() {
        enumValues<Gender>().forEach {
            val rbGender = RadioButton(context)
            rbGender.setText(it.gender)
            binding.rbGender.addView(rbGender)
        }
        (binding.rbGender.getChildAt(0) as RadioButton).isChecked = true
    }

    private fun getSelectedGender(): String {
        val checkedRadioButton =
            binding.root.findViewById<RadioButton>(binding.rbGender.checkedRadioButtonId)
        return checkedRadioButton.text.toString()
    }

    private fun setupDatePicker() {
        calendar = Calendar.getInstance()
        val dateListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }

        binding.etDateOfBirth.setOnClickListener {
            context?.let { context ->
                DatePickerDialog(
                    context, R.style.HancinDatePickerStyle,
                    dateListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
    }

    private fun updateDateInView() {
        val dateFormat = "dd MMMM yyyy"
        val sdf = SimpleDateFormat(dateFormat, Locale.US)
        binding.etDateOfBirth.setText(sdf.format(calendar.time))
    }

    fun setUserInformation() {
        viewModel.setUserInformation(
            binding.etName.text.toString(),
            binding.etIdNumber.text.toString(),
            getSelectedGender(),
            binding.etPlaceOfBirth.text.toString(),
            binding.etDateOfBirth.text.toString(),
            binding.etPhoneNumber.text.toString()
        )
    }

    private fun validateEachField() {
        var isNameValid = false
        var isIdNumberValid = false
        var isPlaceOfBirthValid = false
        var isDateOfBirthValid = false
        var isPhoneNumberValid = false
        binding.etName.addTextChangedListener {
            isNameValid = it.toString().isNotEmpty()
            if (isNameValid)
                binding.tvNameErrorMessage.visibility = View.GONE
            else binding.tvNameErrorMessage.visibility = View.VISIBLE
            viewModel.setIsSectionValid(isNameValid && isIdNumberValid && isPlaceOfBirthValid && isDateOfBirthValid && isPhoneNumberValid)
        }
        binding.etIdNumber.addTextChangedListener {
            isIdNumberValid = it.toString().isNotEmpty() && it.toString().length == 16
            if (isIdNumberValid)
                binding.tvIdNumberErrorMessage.visibility = View.GONE
            else binding.tvIdNumberErrorMessage.visibility = View.VISIBLE
            viewModel.setIsSectionValid(isNameValid && isIdNumberValid && isPlaceOfBirthValid && isDateOfBirthValid && isPhoneNumberValid)
        }
        binding.etPlaceOfBirth.addTextChangedListener {
            isPlaceOfBirthValid = it.toString().isNotEmpty()
            if (isPlaceOfBirthValid)
                binding.tvPlaceOfBirthErrorMessage.visibility = View.GONE
            else binding.tvPlaceOfBirthErrorMessage.visibility = View.VISIBLE
            viewModel.setIsSectionValid(isNameValid && isIdNumberValid && isPlaceOfBirthValid && isDateOfBirthValid && isPhoneNumberValid)
        }
        binding.etDateOfBirth.addTextChangedListener {
            isDateOfBirthValid = it.toString().isNotEmpty()
            if (isDateOfBirthValid)
                binding.tvDateOfBirthErrorMessage.visibility = View.GONE
            else binding.tvPlaceOfBirthErrorMessage.visibility = View.VISIBLE
            viewModel.setIsSectionValid(isNameValid && isIdNumberValid && isPlaceOfBirthValid && isDateOfBirthValid && isPhoneNumberValid)
        }
        binding.etPhoneNumber.addTextChangedListener {
            isPhoneNumberValid = it.toString().isNotEmpty()
            if (isPhoneNumberValid)
                binding.tvPhoneNumberErrorMessage.visibility = View.GONE
            else binding.tvPhoneNumberErrorMessage.visibility = View.VISIBLE
            viewModel.setIsSectionValid(isNameValid && isIdNumberValid && isPlaceOfBirthValid && isDateOfBirthValid && isPhoneNumberValid)
        }
    }

    fun validateSection() {
        viewModel.setIsSectionValid(
            binding.etName.text.toString().isNotEmpty()
                    && binding.etIdNumber.text.toString().isNotEmpty()
                    && binding.etIdNumber.text.toString().length == 16
                    && binding.etPlaceOfBirth.text.toString().isNotEmpty()
                    && binding.etPlaceOfBirth.text.toString().isNotEmpty()
                    && binding.etDateOfBirth.text.toString().isNotEmpty()
                    && binding.etPhoneNumber.text.toString().isNotEmpty()
        )
    }
}