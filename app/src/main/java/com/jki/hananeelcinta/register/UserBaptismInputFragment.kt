package com.jki.hananeelcinta.register

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
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.FragmentUserBaptismInformationInputBinding
import com.jki.hananeelcinta.model.YesNoQuestion
import java.text.SimpleDateFormat
import java.util.*

class UserBaptismInputFragment : Fragment() {

    private lateinit var binding: FragmentUserBaptismInformationInputBinding
    private lateinit var viewModel: RegisterViewModel

    private lateinit var calendar: Calendar

    companion object {
        @JvmStatic
        fun newInstance() = UserBaptismInputFragment().apply { }
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
            R.layout.fragment_user_baptism_information_input,
            container,
            false
        )

        setupWaterBaptismRadioButton()
        setupHolySpiritBaptismRadioButton()
        setupDatePicker()
        validateEachField()

        return binding.root
    }

    private fun setupWaterBaptismRadioButton() {
        enumValues<YesNoQuestion>().forEach {
            val rbWaterBaptism = RadioButton(context)
            rbWaterBaptism.text = it.yesNoQuestion
            binding.rbBaptismInformation.addView(rbWaterBaptism)
        }
        (binding.rbBaptismInformation.getChildAt(0) as RadioButton).isChecked = true
    }

    private fun getWaterBaptismInformation(): String {
        val checkRadioButton =
            binding.root.findViewById<RadioButton>(binding.rbBaptismInformation.checkedRadioButtonId)
        return checkRadioButton.text.toString()
    }

    private fun setupHolySpiritBaptismRadioButton() {
        enumValues<YesNoQuestion>().forEach {
            val rbHSBaptism = RadioButton(context)
            rbHSBaptism.text = it.yesNoQuestion
            binding.rbHolySpiritBaptismInformation.addView(rbHSBaptism)
        }
        (binding.rbHolySpiritBaptismInformation.getChildAt(0) as RadioButton).isChecked = true
    }

    private fun getHSBaptismInformation(): String {
        val checkRadioButton =
            binding.root.findViewById<RadioButton>(binding.rbHolySpiritBaptismInformation.checkedRadioButtonId)
        return checkRadioButton.text.toString()
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

        binding.etBaptismTime.setOnClickListener {
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
        binding.etBaptismTime.setText(sdf.format(calendar.time))
    }

    fun setUserBaptismInformation() {
        viewModel.setUserBaptismInformation(
            getWaterBaptismInformation(),
            binding.etBatpismChurch.text.toString(),
            binding.etBaptismTime.text.toString(),
            getHSBaptismInformation(),
            binding.etChurchOrigin.text.toString(),
            binding.etReasonForMovingChurch.text.toString()
        )
    }

    fun validateSection() {
        viewModel.setIsSectionValid(
            validateWaterBaptism()
                    && binding.etChurchOrigin.text.toString().isNotEmpty()
                    && binding.etReasonForMovingChurch.text.toString().isNotEmpty()
        )
    }

    private fun validateWaterBaptism(): Boolean {
        return if (isUserHasWaterBaptism()) {
            (binding.etBatpismChurch.text.toString().isNotEmpty()
                    && binding.etBaptismTime.text.toString().isNotEmpty())
        } else {
            true
        }
    }

    private fun validateEachField() {
        var isWaterBaptismValid = validateWaterBaptism()
        var isChurchOriginValid = false
        var isReasonMovingChurchValid = false

        binding.rbBaptismInformation.setOnCheckedChangeListener { radioGroup, i ->
            isWaterBaptismValid = validateWaterBaptism()
            viewModel.setIsSectionValid(isWaterBaptismValid && isChurchOriginValid && isReasonMovingChurchValid)
        }
        binding.etChurchOrigin.addTextChangedListener {
            isChurchOriginValid = it.toString().isNotEmpty()
            if (isChurchOriginValid)
                binding.tvChruchOriginErrorMessage.visibility = View.GONE
            else binding.tvChruchOriginErrorMessage.visibility = View.VISIBLE
            viewModel.setIsSectionValid(isWaterBaptismValid && isChurchOriginValid && isReasonMovingChurchValid)
        }
        binding.etReasonForMovingChurch.addTextChangedListener {
            isReasonMovingChurchValid = it.toString().isNotEmpty()
            if (isReasonMovingChurchValid)
                binding.tvReasonMovingChurchErrorMessage.visibility = View.GONE
            else binding.tvReasonMovingChurchErrorMessage.visibility = View.VISIBLE
            viewModel.setIsSectionValid(isWaterBaptismValid && isChurchOriginValid && isReasonMovingChurchValid)
        }
    }

    private fun isUserHasWaterBaptism(): Boolean {
        return getWaterBaptismInformation() == "true"
    }
}