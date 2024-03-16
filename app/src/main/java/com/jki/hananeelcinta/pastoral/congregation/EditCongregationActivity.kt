package com.jki.hananeelcinta.pastoral.congregation

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.autofill.UserData
import android.widget.RadioButton
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityEditCongregationBinding
import com.jki.hananeelcinta.model.BloodType
import com.jki.hananeelcinta.model.Education
import com.jki.hananeelcinta.model.Gender
import com.jki.hananeelcinta.model.Job
import com.jki.hananeelcinta.model.YesNoQuestion
import com.jki.hananeelcinta.util.UserConfiguration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditCongregationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditCongregationBinding

    companion object {
        const val USER_ID_ARG = "userIdArg"
    }

    private val database = FirebaseDatabase.getInstance()
    private val userRef = database.getReference("users")
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference

    private lateinit var userId: String
    private val userData = UserConfiguration.getInstance().getUserData()

    private lateinit var calendar: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_congregation)
        supportActionBar?.hide()
        getAdditionalData()

        setupLayout()
    }

    private fun getAdditionalData() {
        userId = intent.getStringExtra(CongregationDetailActivity.USER_ID_ARG).toString()
    }

    private fun setupLayout() {
        binding.btnBack.setOnClickListener { onBackPressed() }
        setupFieldValue()
    }

    private fun setupGenderRadioButton() {
        enumValues<Gender>().forEach {
            val rbGender = RadioButton(applicationContext)
            rbGender.text = it.gender
            binding.rbGender.addView(rbGender)
        }
        (binding.rbGender.getChildAt(findSelectedGender(userData?.gender)) as RadioButton).isChecked =
            true
    }

    private fun findSelectedGender(currentGender: String?): Int {
        return Gender.values().indexOfFirst { it.gender.equals(currentGender, ignoreCase = true) }
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
            applicationContext?.let { context ->
                DatePickerDialog(
                    context,
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

    private fun setupBloodTypeRadioButton() {
        enumValues<BloodType>().forEach {
            val rbBloodType = RadioButton(applicationContext)
            rbBloodType.text = it.bloodType
            binding.rbBloodType.addView(rbBloodType)
        }
        (binding.rbBloodType.getChildAt(findSelectedBloodType(userData?.bloodType)) as RadioButton).isChecked =
            true
    }

    private fun findSelectedBloodType(currentBloodType: String?): Int {
        return BloodType.values()
            .indexOfFirst { it.bloodType.equals(currentBloodType, ignoreCase = true) }
    }

    private fun setupLastEducationRadioButton() {
        enumValues<Education>().forEach {
            val rbEducation = RadioButton(applicationContext)
            rbEducation.text = it.education
            binding.rbLastEducation.addView(rbEducation)
        }
        (binding.rbLastEducation.getChildAt(findSelectedLastEducation(userData?.lastEducation)) as RadioButton).isChecked =
            true
        if (findSelectedLastEducation(userData?.lastEducation) == -1) binding.etLastEducation.setText(
            userData?.lastEducation
        )
    }

    private fun findSelectedLastEducation(currentLastEducation: String?): Int {
        return if (Education.values()
                .indexOfFirst { it.education.equals(currentLastEducation, ignoreCase = true) } != -1
        ) {
            Education.values()
                .indexOfFirst { it.education.equals(currentLastEducation, ignoreCase = true) }
        } else {
            Education.values().lastIndex
        }
    }

    private fun setupJobRadioButton() {
        enumValues<Job>().forEach {
            val rbJob = RadioButton(applicationContext)
            rbJob.text = it.job
            binding.rbWork.addView(rbJob)
        }
        (binding.rbWork.getChildAt(findSelectedJob(userData?.job)) as RadioButton).isChecked = true
        if (findSelectedJob(userData?.job) == -1) binding.etWork.setText(
            userData?.job
        )
    }

    private fun findSelectedJob(currentJob: String?): Int {
        return if (Job.values()
                .indexOfFirst { it.job.equals(currentJob, ignoreCase = true) } != -1
        ) {
            Job.values()
                .indexOfFirst { it.job.equals(currentJob, ignoreCase = true) }
        } else {
            Job.values().lastIndex
        }
    }

    private fun setupWaterBaptismRadioButton() {
        enumValues<YesNoQuestion>().forEach {
            val rbWaterBaptism = RadioButton(applicationContext)
            rbWaterBaptism.text = it.yesNoQuestion
            binding.rbBaptismInformation.addView(rbWaterBaptism)
        }
        (binding.rbBaptismInformation.getChildAt(findSelectedWaterBaptismInformation(userData?.waterBaptism)) as RadioButton).isChecked =
            true
    }

    private fun findSelectedWaterBaptismInformation(currentStatus: Boolean?): Int {
        val status = if (currentStatus == true) "Sudah" else "Belum"
        return YesNoQuestion.values()
            .indexOfFirst { it.yesNoQuestion.equals(status, ignoreCase = true) }
    }

    private fun setupHolySpiritBaptismRadioButton() {
        enumValues<YesNoQuestion>().forEach {
            val rbHSBaptism = RadioButton(applicationContext)
            rbHSBaptism.text = it.yesNoQuestion
            binding.rbHolySpiritBaptismInformation.addView(rbHSBaptism)
        }
        (binding.rbHolySpiritBaptismInformation.getChildAt(
            findSelectedHolySpiritBaptismInformation(
                userData?.holySpiritBaptism
            )
        ) as RadioButton).isChecked = true
    }

    private fun findSelectedHolySpiritBaptismInformation(currentStatus: Boolean?): Int {
        val status = if (currentStatus == true) "Sudah" else "Belum"
        return YesNoQuestion.values()
            .indexOfFirst { it.yesNoQuestion.equals(status, ignoreCase = true) }
    }

    private fun setupFieldValue() {
        setupGenderRadioButton()
        binding.etPlaceOfBirth.setText(userData?.placeOfBirth)
        binding.etDateOfBirth.setText(userData?.dateOfBirth)
        setupDatePicker()
        binding.etAddress.setText(userData?.address)
        setupBloodTypeRadioButton()
        setupLastEducationRadioButton()
        setupJobRadioButton()
        setupWaterBaptismRadioButton()
        setupHolySpiritBaptismRadioButton()
    }

    private fun editUserData() {
        userData?.gender = getSelectedGender()
        if (binding.etPlaceOfBirth.text.isNotBlank()) userData?.placeOfBirth =
            binding.etPlaceOfBirth.text.toString()
        userData?.dateOfBirth = binding.etDateOfBirth.text.toString()
        if (binding.etAddress.text.isNotBlank()) userData?.address =
            binding.etAddress.text.toString()

    }
}