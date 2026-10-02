package com.jki.myhananeelcinta.register

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jki.myhananeelcinta.R
import com.jki.myhananeelcinta.databinding.FragmentUserInformationInputBinding
import com.jki.myhananeelcinta.model.Gender
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
            rbGender.text = it.gender
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
                    context,
                    dateListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).apply {
                    datePicker.maxDate = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }.timeInMillis - 1
                }.show()
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
            getSelectedGender(),
            binding.etPlaceOfBirth.text.toString(),
            binding.etDateOfBirth.text.toString(),
            getFormattedPhoneNumber(binding.etPhoneNumber.text.toString())
        )
    }

    private fun getFormattedPhoneNumber(phoneNumber: String): String {
        return if (phoneNumber.startsWith("+62")) {
            phoneNumber
        } else if (phoneNumber.startsWith("0")) {
            "+62${phoneNumber.substring(1)}"
        } else {
            phoneNumber
        }
    }

    private fun validateEachField() {
        listOf(binding.etName, binding.etPlaceOfBirth, binding.etDateOfBirth, binding.etPhoneNumber)
            .forEach { field -> field.addTextChangedListener { validateSection() } }
    }

    fun validateSection() {
        val nameValid = binding.etName.text.toString().trim().length in 1..255
        val birthplaceValid = binding.etPlaceOfBirth.text.toString().trim().length <= 100
        val phone = getFormattedPhoneNumber(binding.etPhoneNumber.text.toString().trim())
        val phoneValid = phone.isEmpty() || (phone.length in 7..30 && phone.matches(Regex("[0-9+() .-]+")))
        binding.tvNameErrorMessage.visibility = if (nameValid) View.GONE else View.VISIBLE
        binding.tvPlaceOfBirthErrorMessage.visibility = if (birthplaceValid) View.GONE else View.VISIBLE
        binding.tvPhoneNumberErrorMessage.visibility = if (phoneValid) View.GONE else View.VISIBLE
        viewModel.setIsSectionValid(nameValid && birthplaceValid && phoneValid)
    }
}
