package com.jki.myhananeelcinta.pastoral.congregation

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jki.myhananeelcinta.R
import com.jki.myhananeelcinta.camera.CameraActivity
import com.jki.myhananeelcinta.databinding.ActivityEditCongregationBinding
import com.jki.myhananeelcinta.model.BloodType
import com.jki.myhananeelcinta.model.Education
import com.jki.myhananeelcinta.model.Gender
import com.jki.myhananeelcinta.model.Job
import com.jki.myhananeelcinta.model.YesNoQuestion
import com.jki.myhananeelcinta.util.PictureUploader
import com.jki.myhananeelcinta.util.UIHelper
import com.jki.myhananeelcinta.util.UserConfiguration
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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

    private val profilePictureUploader = PictureUploader()
    private var capturedImageFile: File? = null

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
        loadProfileImage()

        binding.btnSubmit.setOnClickListener {
            editUserData()
        }

        binding.ivEdit.setOnClickListener {
            editProfilePicture()
        }
    }

    private fun editProfilePicture() {
        val intent = Intent(applicationContext, CameraActivity::class.java)
        getResult.launch(intent)
    }

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val capturedImageData =
                    result.data?.getByteArrayExtra(CameraActivity.ARG_CAMERA_RESULT)
                capturedImageData?.let { imageData ->
                    val capturedImage = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                    capturedImageFile =
                        getProfilePictureFile(
                            "profile-picture.jpg",
                            capturedImage
                        )
                    binding.ivProfile.setImageBitmap(capturedImage)
                }
            }
        }

    private fun getProfilePictureFile(fileName: String, bitmap: Bitmap): File? {
        val file = File(applicationContext.cacheDir, fileName)
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            return file
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun loadProfileImage() {
        val profilePicturesRef = storageRef.child("${userId}/profile-pictures")
        profilePicturesRef.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.getResult()
                val imageUrl = downloadUri.toString()
                val requestOptions = RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both the original and resized image
                    .centerCrop() // Center-crop the image to fit the ImageView

                Glide.with(applicationContext)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_no_profile_image)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .apply(requestOptions)
                    .into(binding.ivProfile)
            }
        }
    }

    private fun setupGenderRadioButton() {
        enumValues<Gender>().forEach {
            val color = ContextCompat.getColor(this, R.color.color_primary)
            val rbGender = RadioButton(applicationContext)
            rbGender.text = it.gender
            rbGender.setTextColor(resources.getColor(R.color.black))
            rbGender.buttonTintList = ColorStateList.valueOf(color)
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
            applicationContext?.let {
                DatePickerDialog(
                    this,
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
            val color = ContextCompat.getColor(this, R.color.color_primary)
            val rbBloodType = RadioButton(applicationContext)
            rbBloodType.text = it.bloodType
            rbBloodType.setTextColor(resources.getColor(R.color.black))
            rbBloodType.buttonTintList = ColorStateList.valueOf(color)
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
            val color = ContextCompat.getColor(this, R.color.color_primary)
            val rbEducation = RadioButton(applicationContext)
            rbEducation.text = it.education
            rbEducation.setTextColor(resources.getColor(R.color.black))
            rbEducation.buttonTintList = ColorStateList.valueOf(color)
            binding.rbLastEducation.addView(rbEducation)
        }
        (binding.rbLastEducation.getChildAt(findSelectedLastEducation(userData?.lastEducation)) as RadioButton).isChecked =
            true
        if (findSelectedLastEducation(userData?.lastEducation) == Education.values().lastIndex) {
            val selectedEdu = userData?.lastEducation
            if (selectedEdu != null) {
                binding.etLastEducation.setText(
                    selectedEdu.substring(selectedEdu.indexOf(" - ") + 3)
                )
            }
        }
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
            val color = ContextCompat.getColor(this, R.color.color_primary)
            val rbJob = RadioButton(applicationContext)
            rbJob.text = it.job
            rbJob.setTextColor(resources.getColor(R.color.black))
            rbJob.buttonTintList = ColorStateList.valueOf(color)
            binding.rbWork.addView(rbJob)
        }
        (binding.rbWork.getChildAt(findSelectedJob(userData?.job)) as RadioButton).isChecked = true
        if (findSelectedJob(userData?.job) == Job.values().lastIndex) {
            val selectedJob = userData?.job
            if (selectedJob != null) {
                binding.etWork.setText(
                    selectedJob.substring(selectedJob.indexOf(" - ") + 3)
                )
            }
        }
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
        val color = ContextCompat.getColor(this, R.color.color_primary)
        enumValues<YesNoQuestion>().forEach {
            val rbWaterBaptism = RadioButton(applicationContext)
            rbWaterBaptism.text = it.yesNoQuestion
            rbWaterBaptism.setTextColor(resources.getColor(R.color.black))
            rbWaterBaptism.buttonTintList = ColorStateList.valueOf(color)
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
            val color = ContextCompat.getColor(this, R.color.color_primary)
            val rbHSBaptism = RadioButton(applicationContext)
            rbHSBaptism.text = it.yesNoQuestion
            rbHSBaptism.setTextColor(resources.getColor(R.color.black))
            rbHSBaptism.buttonTintList = ColorStateList.valueOf(color)
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

    private fun getSelectedBloodType(): String {
        val checkedRadioButton =
            binding.root.findViewById<RadioButton>(binding.rbBloodType.checkedRadioButtonId)
        return checkedRadioButton.text.toString()
    }

    private fun getSelectedEducation(): String {
        val checkRadioButton =
            binding.root.findViewById<RadioButton>(binding.rbLastEducation.checkedRadioButtonId)

        if (checkRadioButton.text.toString() == "Lainnya") {
            return checkRadioButton.text.toString() + " - " + binding.etLastEducation.text
        }

        return checkRadioButton.text.toString()
    }

    private fun getSelectedJob(): String {
        val checkedRadioButton =
            binding.root.findViewById<RadioButton>(binding.rbWork.checkedRadioButtonId)

        if (checkedRadioButton.text.toString().equals("Lainnya")) {
            return checkedRadioButton.text.toString() + " - " + binding.etWork.text
        }

        return checkedRadioButton.text.toString()
    }

    private fun getWaterBaptismInformation(): Boolean {
        val checkRadioButton =
            binding.root.findViewById<RadioButton>(binding.rbBaptismInformation.checkedRadioButtonId)
        return checkRadioButton.text.toString() == "Sudah"
    }

    private fun getHSBaptismInformation(): Boolean {
        val checkRadioButton =
            binding.root.findViewById<RadioButton>(binding.rbHolySpiritBaptismInformation.checkedRadioButtonId)
        return checkRadioButton.text.toString() == "Sudah"
    }

    private fun setupFieldValue() {
        binding.model = userData
        setupGenderRadioButton()
        binding.etPhoneNumber.setText(userData?.phoneNumber)
        binding.etPlaceOfBirth.setText(userData?.placeOfBirth)
        binding.etDateOfBirth.setText(userData?.dateOfBirth)
        setupDatePicker()
        binding.etAddress.setText(userData?.address)
        setupBloodTypeRadioButton()
        setupLastEducationRadioButton()
        setupJobRadioButton()
        setupWaterBaptismRadioButton()
        setupHolySpiritBaptismRadioButton()
        binding.etBaptismChurch.setText(userData?.waterBaptisteryChurch)
        binding.etChurchOrigin.setText(userData?.churchOrigin)
        binding.etReasonForMovingChurch.setText(userData?.reasonToMovingChurch)
    }

    private fun editUserData() {
        binding.isLoading = true
        userData?.gender = getSelectedGender()
        if (binding.etPlaceOfBirth.text.isNotBlank()) userData?.placeOfBirth =
            binding.etPlaceOfBirth.text.toString()
        userData?.dateOfBirth = binding.etDateOfBirth.text.toString()
        if (binding.etAddress.text.isNotBlank()) userData?.address =
            binding.etAddress.text.toString()
        userData?.bloodType = getSelectedBloodType()
        userData?.lastEducation = getSelectedEducation()
        userData?.job = getSelectedJob()
        userData?.waterBaptism = getWaterBaptismInformation()
        userData?.holySpiritBaptism = getHSBaptismInformation()
        userData?.reasonToMovingChurch = binding.etReasonForMovingChurch.text.toString()

        userRef.child(userId).setValue(userData)
            .addOnSuccessListener {
                UserConfiguration.getInstance().setUserData(userData!!)
                if (capturedImageFile != null) {
                    uploadProfilePicture()
                } else {
                    showSuccessDialog()
                }
            }.addOnFailureListener {
                binding.isLoading = false
                Toast.makeText(applicationContext, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadProfilePicture() {
        if (capturedImageFile != null) {
            profilePictureUploader.uploadProfilePicture(capturedImageFile!!.toUri()) { imageUrl, error ->
                if (error != null) {
                    Toast.makeText(applicationContext, error, Toast.LENGTH_SHORT).show()
                } else {
                    showSuccessDialog()
                }
            }
        } else {
            Toast.makeText(applicationContext, "Fail to upload profile picture", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun showSuccessDialog() {
        binding.isLoading = false
        UIHelper.getInstance().displaySuccessDialog(
            resources.getString(R.string.success_user_registration_title),
            resources.getString(R.string.success_user_registration_desc),
            this,
            {
                finish()
            },
            resources.getString(R.string.OK),
            false,
        )
    }
}