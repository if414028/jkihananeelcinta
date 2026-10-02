package com.jki.myhananeelcinta.register

import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import com.jki.myhananeelcinta.camera.CameraActivity
import com.jki.myhananeelcinta.model.BloodType
import com.jki.myhananeelcinta.model.Education
import com.jki.myhananeelcinta.model.FamilyStatus
import com.jki.myhananeelcinta.model.Job
import java.io.File
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.jki.myhananeelcinta.R
import com.jki.myhananeelcinta.databinding.ActivityRegisterBinding
import com.jki.myhananeelcinta.home.MainActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_IMAGE_SELFIE = 1
    }

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel
    private val form get() = viewModel.form
    private lateinit var fields: Map<String, Pair<EditText, TextInputLayout>>
    private val errorMessages = mapOf(
        "email" to R.string.reg_error_email, "password" to R.string.reg_error_password,
        "confirmation" to R.string.reg_error_confirmation, "name" to R.string.reg_error_name,
        "nickname" to R.string.reg_error_nickname, "phone" to R.string.reg_error_phone,
        "birthplace" to R.string.reg_error_birthplace, "birthdate" to R.string.reg_error_birthdate,
        "address" to R.string.reg_error_address, "occupation" to R.string.reg_error_occupation,
        "baptism_date" to R.string.reg_error_baptism_date,
        "blood" to R.string.reg_error_detail, "education" to R.string.reg_error_detail,
        "baptism_church" to R.string.reg_error_detail, "church_origin" to R.string.reg_error_detail,
        "moving_reason" to R.string.reg_error_detail, "family_status" to R.string.reg_error_detail,
        "wife" to R.string.reg_error_detail, "husband" to R.string.reg_error_detail,
        "children" to R.string.reg_error_detail, "siblings" to R.string.reg_error_detail
    )
    private val maritalValues = listOf("", "single", "married", "widowed", "divorced")
    private val baptismValues = listOf("unknown", "not_baptized", "baptized")
    private lateinit var maritalLabels: List<String>
    private lateinit var baptismLabels: List<String>
    private val lastStep = 5
    private val stepNames = listOf(R.string.reg_step_account, R.string.reg_step_personal, R.string.reg_step_profile,
        R.string.reg_step_church, R.string.reg_step_family, R.string.reg_step_review)

    private val takePhoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getByteArrayExtra(CameraActivity.ARG_CAMERA_RESULT)?.let(::saveCapturedPhoto)
        }
    }

    private fun saveCapturedPhoto(bytes: ByteArray) {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        if (bitmap == null) { showPhotoError(R.string.reg_photo_failed); return }
        val previous = viewModel.capturedImageFile
        var newFile: File? = null
        val result = runCatching {
            File.createTempFile("registration-photo-", ".jpg", filesDir).also { file ->
                newFile = file
                file.outputStream().use { require(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)) }
                require(file.length() in 1..5_242_880)
            }
        }
        bitmap.recycle()
        result.fold({ file ->
            viewModel.capturedImageFile = file
            form.photoPath = file.name
            previous?.delete()
            showPhoto()
        }, { newFile?.delete(); showPhotoError(R.string.reg_photo_failed) })
    }

    private fun showPhotoError(message: Int) {
        binding.tvPhotoError.setText(message)
        binding.tvPhotoError.visibility = View.VISIBLE
    }

    private fun showPhoto() {
        val file = viewModel.capturedImageFile?.takeIf { it.isFile }
        if (file == null) binding.ivPhoto.setImageResource(R.drawable.grey_circle)
        else binding.ivPhoto.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
        binding.btnTakePhoto.setText(if (file == null) R.string.reg_take_photo else R.string.reg_retake_photo)
        binding.btnRemovePhoto.visibility = if (file == null) View.GONE else View.VISIBLE
        binding.tvPhotoError.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Hancin_Register)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]
        supportActionBar?.hide()
        setupInsets()
        fields = mapOf(
            "email" to (binding.etEmail to binding.tilEmail),
            "password" to (binding.etPassword to binding.tilPassword),
            "confirmation" to (binding.etConfirmation to binding.tilConfirmation),
            "name" to (binding.etName to binding.tilName),
            "nickname" to (binding.etNickname to binding.tilNickname),
            "phone" to (binding.etPhone to binding.tilPhone),
            "birthplace" to (binding.etBirthplace to binding.tilBirthplace),
            "birthdate" to (binding.etBirthdate to binding.tilBirthdate),
            "address" to (binding.etAddress to binding.tilAddress),
            "occupation" to (binding.etOccupation to binding.tilOccupation),
            "baptism_date" to (binding.etBaptismDate to binding.tilBaptismDate),
            "blood" to (binding.etBlood to binding.tilBlood), "education" to (binding.etEducation to binding.tilEducation),
            "baptism_church" to (binding.etBaptismChurch to binding.tilBaptismChurch),
            "church_origin" to (binding.etChurchOrigin to binding.tilChurchOrigin),
            "moving_reason" to (binding.etMovingReason to binding.tilMovingReason),
            "family_status" to (binding.etFamilyStatus to binding.tilFamilyStatus),
            "wife" to (binding.etWife to binding.tilWife), "husband" to (binding.etHusband to binding.tilHusband),
            "children" to (binding.etChildren to binding.tilChildren), "siblings" to (binding.etSiblings to binding.tilSiblings)
        )
        if (savedInstanceState != null) {
            form.step = savedInstanceState.getInt("register_step", form.step)
            form.birthdate = savedInstanceState.getString("birthdate", form.birthdate).orEmpty()
            form.baptismDate = savedInstanceState.getString("baptism_date", form.baptismDate).orEmpty()
            form.photoPath = savedInstanceState.getString("photo_path", form.photoPath).orEmpty()
        }
        if (form.photoPath.isNotBlank() && viewModel.capturedImageFile == null) {
            // Only app-private photo files can be restored; no external path is trusted.
            File(filesDir, form.photoPath).takeIf {
                it.parentFile?.canonicalFile == filesDir.canonicalFile && it.isFile
            }?.let { viewModel.capturedImageFile = it }
        }
        setupChoices()
        restoreDraft()
        showPhoto()
        setupListeners()
        showStep(form.step, animate = false)
        observeRegistration()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goBack()
        })
    }

    private fun setupInsets() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.register_surface)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.register_surface)
        val dark = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(window, binding.root).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, keyboard.bottom))
            binding.tvFooterNote.visibility = if (insets.isVisible(WindowInsetsCompat.Type.ime()) || resources.configuration.fontScale >= 1.5f) View.GONE else View.VISIBLE
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun setupChoices() {
        maritalLabels = listOf(R.string.reg_not_filled, R.string.reg_single, R.string.reg_married,
            R.string.reg_widowed, R.string.reg_divorced).map { getString(it) }
        baptismLabels = listOf(R.string.reg_baptism_unknown, R.string.reg_not_baptized, R.string.reg_baptized).map { getString(it) }
        binding.etMarital.setAdapter(ArrayAdapter(this, R.layout.item_register_choice, maritalLabels))
        binding.etBaptism.setAdapter(ArrayAdapter(this, R.layout.item_register_choice, baptismLabels))
        val unset = getString(R.string.reg_not_filled)
        binding.etBlood.setAdapter(ArrayAdapter(this, R.layout.item_register_choice, listOf(unset) + BloodType.values().map { it.bloodType }))
        binding.etEducation.setAdapter(ArrayAdapter(this, R.layout.item_register_choice, listOf(unset) + Education.values().map { it.education }))
        binding.etOccupation.setAdapter(ArrayAdapter(this, R.layout.item_register_choice, Job.values().map { it.job }))
        binding.etFamilyStatus.setAdapter(ArrayAdapter(this, R.layout.item_register_choice, listOf(unset) + FamilyStatus.values().map { it.familyStatus }))
        binding.etHolySpirit.setAdapter(ArrayAdapter(this, R.layout.item_register_choice, listOf(unset, getString(R.string.reg_holy_yes), getString(R.string.reg_holy_no))))
        binding.etBaptism.setOnItemClickListener { _, _, position, _ ->
            form.baptismStatus = baptismValues[position]
            binding.tilBaptismDate.visibility = if (form.baptismStatus == "baptized") View.VISIBLE else View.GONE
            if (form.baptismStatus != "baptized") binding.tilBaptismDate.error = null
        }
    }

    private fun restoreDraft() {
        binding.etEmail.setText(form.email)
        binding.etPassword.setText(form.password)
        binding.etConfirmation.setText(form.confirmation)
        binding.etName.setText(form.fullName)
        binding.etNickname.setText(form.nickname)
        binding.etPhone.setText(form.phone)
        binding.etBirthplace.setText(form.birthplace)
        binding.etBirthdate.setText(displayDate(form.birthdate))
        binding.etAddress.setText(form.address)
        binding.etOccupation.setText(form.occupation)
        binding.etBaptismDate.setText(displayDate(form.baptismDate))
        binding.etBlood.setText(form.bloodType, false)
        binding.etEducation.setText(form.education, false)
        binding.etBaptismChurch.setText(form.baptismChurch)
        binding.etHolySpirit.setText(form.holySpiritStatus, false)
        binding.etChurchOrigin.setText(form.churchOrigin)
        binding.etMovingReason.setText(form.movingReason)
        binding.etFamilyStatus.setText(form.familyStatus, false)
        binding.etWife.setText(form.wifeName)
        binding.etHusband.setText(form.husbandName)
        binding.etChildren.setText(form.childrenNames)
        binding.etSiblings.setText(form.siblingsNames)
        binding.etMarital.setText(maritalLabels[maritalValues.indexOf(form.maritalStatus).coerceAtLeast(0)], false)
        binding.etBaptism.setText(baptismLabels[baptismValues.indexOf(form.baptismStatus).coerceAtLeast(0)], false)
        when (form.gender) {
            "male" -> binding.rgGender.check(R.id.rb_male)
            "female" -> binding.rgGender.check(R.id.rb_female)
        }
        binding.tilBaptismDate.visibility = if (form.baptismStatus == "baptized") View.VISIBLE else View.GONE
    }

    private fun setupListeners() {
        binding.btnContinue.setOnClickListener { continueRegistration() }
        binding.btnBack.setOnClickListener { goBack() }
        binding.btnLogin.setOnClickListener { leaveRegistration() }
        binding.btnEditAccount.setOnClickListener { collectDraft(); showStep(0) }
        binding.btnEditPersonal.setOnClickListener { collectDraft(); showStep(1) }
        binding.btnEditProfile.setOnClickListener { collectDraft(); showStep(2) }
        binding.btnEditChurch.setOnClickListener { collectDraft(); showStep(3) }
        binding.btnEditFamily.setOnClickListener { collectDraft(); showStep(4) }
        binding.btnTakePhoto.setOnClickListener {
            if (viewModel.isSubmitting.value != true) {
                collectDraft()
                hideKeyboard()
                takePhoto.launch(Intent(this, CameraActivity::class.java))
            }
        }
        binding.btnRemovePhoto.setOnClickListener {
            viewModel.capturedImageFile?.delete()
            viewModel.capturedImageFile = null
            form.photoPath = ""
            showPhoto()
        }
        listOf(binding.etBlood, binding.etEducation, binding.etBaptismChurch, binding.etHolySpirit,
            binding.etChurchOrigin, binding.etMovingReason, binding.etFamilyStatus, binding.etWife,
            binding.etHusband, binding.etChildren, binding.etSiblings).forEach { edit ->
            edit.addTextChangedListener { binding.tvServerError.visibility = View.GONE }
        }
        binding.etBirthdate.setOnClickListener { pickDate(false) }
        binding.tilBirthdate.setEndIconOnClickListener { pickDate(false) }
        binding.etBaptismDate.setOnClickListener { pickDate(true) }
        binding.tilBaptismDate.setEndIconOnClickListener { pickDate(true) }
        binding.rgGender.setOnCheckedChangeListener { _, _ -> binding.tvGenderError.visibility = View.GONE }
        fields.forEach { (name, pair) ->
            val (edit, layout) = pair
            edit.addTextChangedListener {
                if (layout.error != null) {
                    collectDraft()
                    if (name !in form.errors(form.step)) layout.error = null
                }
                binding.tvServerError.visibility = View.GONE
            }
            edit.setOnFocusChangeListener { _, focused ->
                if (!focused && edit.text.isNotBlank()) {
                    collectDraft()
                    if (name in form.errors(form.step)) layout.error = getString(errorMessages.getValue(name))
                }
            }
        }
        listOf(binding.etConfirmation, binding.etPhone).forEach { edit ->
            edit.setOnEditorActionListener { _, action, _ ->
                if (action == EditorInfo.IME_ACTION_DONE) { continueRegistration(); true } else false
            }
        }
    }

    private fun collectDraft() {
        form.email = binding.etEmail.text.toString().trim()
        form.password = binding.etPassword.text.toString()
        form.confirmation = binding.etConfirmation.text.toString()
        form.fullName = binding.etName.text.toString()
        form.gender = when (binding.rgGender.checkedRadioButtonId) {
            R.id.rb_male -> "male"
            R.id.rb_female -> "female"
            else -> ""
        }
        form.nickname = binding.etNickname.text.toString()
        form.phone = binding.etPhone.text.toString()
        form.birthplace = binding.etBirthplace.text.toString()
        form.address = binding.etAddress.text.toString()
        form.occupation = binding.etOccupation.text.toString()
        fun choice(edit: EditText) = edit.text.toString().takeUnless { it == getString(R.string.reg_not_filled) }.orEmpty()
        form.bloodType = choice(binding.etBlood)
        form.education = choice(binding.etEducation)
        form.baptismChurch = binding.etBaptismChurch.text.toString()
        form.holySpiritStatus = choice(binding.etHolySpirit)
        form.churchOrigin = binding.etChurchOrigin.text.toString()
        form.movingReason = binding.etMovingReason.text.toString()
        form.familyStatus = choice(binding.etFamilyStatus)
        form.wifeName = binding.etWife.text.toString()
        form.husbandName = binding.etHusband.text.toString()
        form.childrenNames = binding.etChildren.text.toString()
        form.siblingsNames = binding.etSiblings.text.toString()
        form.maritalStatus = maritalValues.getOrElse(maritalLabels.indexOf(binding.etMarital.text.toString())) { "" }
        form.baptismStatus = baptismValues.getOrElse(baptismLabels.indexOf(binding.etBaptism.text.toString())) { "unknown" }
    }

    private fun continueRegistration() {
        if (viewModel.isSubmitting.value == true) return
        collectDraft()
        if (!validateStep(form.step)) return
        hideKeyboard()
        if (form.step < lastStep) showStep(form.step + 1)
        else {
            if (form.photoPath.isNotBlank() && viewModel.capturedImageFile?.isFile != true) {
                showPhotoError(R.string.reg_photo_missing)
                binding.formScroll.post { binding.formScroll.smoothScrollTo(0, binding.panelReview.top) }
                return
            }
            val invalidStep = (0..lastStep).firstOrNull { form.errors(it).isNotEmpty() }
            if (invalidStep != null) { showStep(invalidStep); validateStep(invalidStep); return }
            binding.tvServerError.visibility = View.GONE
            viewModel.submitForm()
        }
    }

    private fun validateStep(step: Int): Boolean {
        val errors = form.errors(step)
        fields.forEach { (name, pair) ->
            pair.second.error = if (name in errors) getString(errorMessages.getValue(name)) else null
        }
        binding.tvGenderError.visibility = if ("gender" in errors) View.VISIBLE else View.GONE
        if (errors.isNotEmpty()) {
            val first = fields[errors.first()]?.first ?: binding.rbMale
            first.requestFocus()
            binding.formScroll.post {
                val rect = android.graphics.Rect()
                first.getDrawingRect(rect)
                binding.formContent.offsetDescendantRectToMyCoords(first, rect)
                binding.formScroll.smoothScrollTo(0, (rect.top - 32 * resources.displayMetrics.density).toInt().coerceAtLeast(0))
            }
        }
        return errors.isEmpty()
    }

    private fun showStep(step: Int, animate: Boolean = true, resetScroll: Boolean = true) {
        form.step = step.coerceIn(0, lastStep)
        val panels = listOf(binding.panelAccount, binding.panelPersonal, binding.panelProfile, binding.panelChurch, binding.panelFamily, binding.panelReview)
        panels.forEach { it.animate().cancel(); it.visibility = View.GONE; it.alpha = 1f; it.translationY = 0f }
        val current = panels[form.step]
        current.visibility = View.VISIBLE
        val titles = listOf(R.string.reg_title_account, R.string.reg_title_personal, R.string.reg_title_profile, R.string.reg_title_church, R.string.reg_title_family, R.string.reg_title_review)
        val descriptions = listOf(R.string.reg_desc_account, R.string.reg_desc_personal, R.string.reg_desc_profile, R.string.reg_desc_church, R.string.reg_desc_family, R.string.reg_desc_review)
        val notes = listOf(R.string.reg_note_account, R.string.reg_note_personal, R.string.reg_note_profile, R.string.reg_note_church, R.string.reg_note_family, R.string.reg_note_review)
        binding.tvTitle.setText(titles[form.step])
        val largeText = resources.configuration.fontScale >= 1.5f
        val shortDescriptions = listOf(R.string.reg_short_account, R.string.reg_short_personal, R.string.reg_short_profile, R.string.reg_short_church, R.string.reg_short_family, R.string.reg_short_review)
        binding.tvTitle.textSize = if (largeText) 24f else 34f
        binding.tvDescription.setText(if (largeText) shortDescriptions[form.step] else descriptions[form.step])
        binding.stepLabels.visibility = if (largeText) View.GONE else View.VISIBLE
        binding.tvFooterNote.setText(notes[form.step])
        binding.tvProgress.text = if (largeText) getString(R.string.reg_progress_accessible, form.step + 1, getString(stepNames[form.step]))
            else getString(R.string.reg_progress, form.step + 1)
        binding.stepProgress.setProgressCompat(form.step + 1, animate && animationsEnabled())
        listOf(binding.tvStepAccount, binding.tvStepPersonal, binding.tvStepReview).forEachIndexed { index, text ->
            val nearbyStep = (form.step - 1).coerceIn(0, lastStep - 2) + index
            text.setText(stepNames[nearbyStep])
            text.setTextColor(ContextCompat.getColor(this, if (nearbyStep == form.step) R.color.register_accent else R.color.register_secondary))
            text.typeface = ResourcesCompat.getFont(this, if (nearbyStep == form.step) R.font.semibold else R.font.regular)
        }
        binding.btnContinue.setText(if (form.step == lastStep) R.string.reg_create else R.string.reg_continue)
        binding.tvServerError.visibility = View.GONE
        if (form.step == lastStep) {
            binding.tvReviewName.text = form.fullName.trim()
            binding.tvReviewEmail.text = form.email
            binding.tvReviewGender.setText(if (form.gender == "male") R.string.reg_male else R.string.reg_female)
            val details = listOf(
                R.string.reg_nickname to form.nickname, R.string.reg_phone to form.formattedPhone(),
                R.string.reg_birthplace to form.birthplace, R.string.reg_birthdate to displayDate(form.birthdate),
                R.string.reg_address to form.address, R.string.reg_blood to form.bloodType,
                R.string.reg_education to form.education, R.string.reg_occupation to form.occupation,
                R.string.reg_baptism to binding.etBaptism.text.toString(), R.string.reg_baptism_date to if (form.baptismStatus == "baptized") displayDate(form.baptismDate) else "",
                R.string.reg_baptism_church to form.baptismChurch, R.string.reg_holy_spirit to form.holySpiritStatus,
                R.string.reg_church_origin to form.churchOrigin, R.string.reg_moving_reason to form.movingReason,
                R.string.reg_marital to binding.etMarital.text.toString(), R.string.reg_family_status to form.familyStatus,
                R.string.reg_wife to form.wifeName, R.string.reg_husband to form.husbandName,
                R.string.reg_children to form.childrenNames, R.string.reg_siblings to form.siblingsNames
            ).filter { it.second.isNotBlank() && it.second != getString(R.string.reg_not_filled) }
            binding.tvReviewDetails.text = details.joinToString("\n\n") { (label, value) -> "${getString(label)}: $value" }
            binding.tvReviewDetails.visibility = if (details.isEmpty()) View.GONE else View.VISIBLE
        }
        if (resetScroll) {
            binding.formScroll.scrollTo(0, 0)
            binding.root.requestFocus()
        }
        if (animate && animationsEnabled()) {
            current.alpha = 0f
            current.translationY = 8 * resources.displayMetrics.density
            current.animate().alpha(1f).translationY(0f).setDuration(180).start()
        }
    }

    private fun pickDate(baptism: Boolean) {
        if (viewModel.isSubmitting.value == true) return
        hideKeyboard()
        val calendar = Calendar.getInstance()
        val current = if (baptism) form.baptismDate else form.birthdate
        if (current.isNotEmpty()) runCatching {
            calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(current)!!
        }
        val dialog = DatePickerDialog(this, { _, year, month, day ->
            val date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
            if (baptism) {
                form.baptismDate = date
                binding.etBaptismDate.setText(displayDate(date))
            } else {
                form.birthdate = date
                binding.etBirthdate.setText(displayDate(date))
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.maxDate = if (baptism) System.currentTimeMillis() else Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis - 1
        dialog.setButton(android.content.DialogInterface.BUTTON_NEUTRAL, getString(R.string.reg_clear_date)) { _, _ ->
            if (baptism) { form.baptismDate = ""; binding.etBaptismDate.setText("") }
            else { form.birthdate = ""; binding.etBirthdate.setText("") }
        }
        dialog.show()
    }

    private fun displayDate(value: String): String = if (value.isBlank()) "" else runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(value)?.let {
            SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(it)
        }.orEmpty()
    }.getOrDefault("")

    private fun observeRegistration() {
        viewModel.isSubmitting.observe(this) { saving ->
            binding.savingStatus.visibility = if (saving) View.VISIBLE else View.GONE
            setInputsEnabled(binding.formContent, !saving)
            binding.btnContinue.isEnabled = !saving
            binding.btnBack.isEnabled = !saving
            binding.btnLogin.isEnabled = !saving
            binding.btnContinue.setText(if (saving) R.string.reg_creating else if (form.step == lastStep) R.string.reg_create else R.string.reg_continue)
        }
        viewModel.isSuccessCreateNewUser.observe(this) {
            binding.etPassword.setText("")
            binding.etConfirmation.setText("")
            Toast.makeText(this, R.string.reg_welcome, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
        viewModel.isFailCreateNewUser.observe(this) { showServerError(it) }
        viewModel.isUserAlreadyRegistered.observe(this) { showServerError(it) }
    }

    private fun showServerError(message: String) {
        binding.tvServerError.text = message
        binding.tvServerError.visibility = View.VISIBLE
        binding.formScroll.post { binding.formScroll.fullScroll(View.FOCUS_DOWN) }
    }

    private fun setInputsEnabled(view: View, enabled: Boolean) {
        if (view is EditText || view is android.widget.Button || view is TextInputLayout) view.isEnabled = enabled
        if (view is ViewGroup) for (index in 0 until view.childCount) setInputsEnabled(view.getChildAt(index), enabled)
    }

    private fun goBack() {
        if (viewModel.isSubmitting.value == true) return
        collectDraft()
        hideKeyboard()
        if (form.step > 0) showStep(form.step - 1) else leaveRegistration()
    }

    private fun leaveRegistration() {
        if (viewModel.isSubmitting.value == true) return
        collectDraft()
        if (form.copy(step = 0) == RegistrationForm()) finish()
        else MaterialAlertDialogBuilder(this)
            .setTitle(R.string.reg_cancel_title).setMessage(R.string.reg_cancel_description)
            .setNegativeButton(R.string.reg_stay, null).setPositiveButton(R.string.reg_leave) { _, _ -> finish() }.show()
    }

    private fun hideKeyboard() {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(binding.root.windowToken, 0)
        currentFocus?.clearFocus()
    }

    private fun animationsEnabled(): Boolean = if (Build.VERSION.SDK_INT >= 26) ValueAnimator.areAnimatorsEnabled()
        else Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f

    override fun onSaveInstanceState(outState: Bundle) {
        collectDraft()
        outState.putInt("register_step", form.step)
        outState.putString("birthdate", form.birthdate)
        outState.putString("baptism_date", form.baptismDate)
        outState.putString("photo_path", form.photoPath)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Android restores non-secret input after onCreate, including after process recreation.
        collectDraft()
        binding.tilBaptismDate.visibility = if (form.baptismStatus == "baptized") View.VISIBLE else View.GONE
        showStep(form.step, animate = false, resetScroll = false)
    }

    override fun onDestroy() {
        if (isFinishing && viewModel.isSubmitting.value != true) viewModel.capturedImageFile?.delete()
        super.onDestroy()
    }

    override fun onStop() {
        collectDraft()
        super.onStop()
    }
}
