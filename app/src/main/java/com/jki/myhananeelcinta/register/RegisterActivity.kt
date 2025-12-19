package com.jki.myhananeelcinta.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.jki.myhananeelcinta.home.MainActivity
import com.jki.myhananeelcinta.R
import com.jki.myhananeelcinta.databinding.ActivityRegisterBinding
import com.jki.myhananeelcinta.stepper.HancinStepperAdapter
import com.jki.myhananeelcinta.stepper.HancinStepperLayout
import com.jki.myhananeelcinta.stepper.contract.HancinStepperContract
import com.jki.myhananeelcinta.util.UIHelper

class RegisterActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_IMAGE_SELFIE = 1;
    }

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel
    private lateinit var stepperAdapter: HancinStepperAdapter
    private lateinit var stepperLayout: HancinStepperLayout

    private lateinit var userCredentialInputFragment: UserCredentialInputFragment
    private lateinit var userInformationInputFragment: UserInformationInputFragment
    private lateinit var userAddressInputFragment: UserAddressInputFragment
    private lateinit var userEducationInputFragment: UserEducationInputFragment
    private lateinit var userBaptismInputFragment: UserBaptismInputFragment
    private lateinit var userMartialInputFragment: UserMartialInputFragment
    private lateinit var userFamilyMemberFragment: UserFamilyMemberInputFragment
    private lateinit var userSelfieInputFragment: UserSelfieInputFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]
        supportActionBar?.hide()

        setupStepperAdapter()
        observeViewModel()
    }

    private fun setupStepperAdapter() {
        stepperLayout = binding.stepperContainer
        stepperLayout.listener = object : HancinStepperContract.Event {
            override fun onSubmitClicked() {
                showLoading(true)
                setResult(HancinStepperLayout.Direction.NEXT)
                viewModel.signUpUser()
            }

            override fun setResult(direction: HancinStepperLayout.Direction?) {
                when (stepperAdapter.selectedSection) {
                    is UserCredentialInputFragment -> {
                        userCredentialInputFragment.setUserCredential()
                    }

                    is UserInformationInputFragment -> {
                        userInformationInputFragment.setUserInformation()
                    }

                    is UserAddressInputFragment -> {
                        userAddressInputFragment.setUserAddress()
                    }

                    is UserEducationInputFragment -> {
                        userEducationInputFragment.setUserEducation()
                    }

                    is UserBaptismInputFragment -> {
                        userBaptismInputFragment.setUserBaptismInformation()
                    }

                    is UserMartialInputFragment -> {
                        userMartialInputFragment.setMartialStatusInformation()
                    }

                    is UserFamilyMemberInputFragment -> {
                        userFamilyMemberFragment.setFamilyMemberData()
                    }

                    is UserSelfieInputFragment -> {

                    }
                }
            }

            override fun onSectionLoaded(direction: HancinStepperLayout.Direction?) {
                when (stepperAdapter.selectedSection) {
                    is UserCredentialInputFragment -> {
                        userCredentialInputFragment.validateSection()
                    }

                    is UserInformationInputFragment -> {
                        userInformationInputFragment.validateSection()
                    }

                    is UserAddressInputFragment -> {
                        userAddressInputFragment.validateSection()
                    }

                    is UserEducationInputFragment -> {
                        stepperLayout.setStepperButtonEnabled(true)
                    }

                    is UserBaptismInputFragment -> {
                        userBaptismInputFragment.validateSection()
                    }

                    is UserMartialInputFragment -> {
                        userMartialInputFragment.validateSection()
                    }

                    is UserFamilyMemberInputFragment -> {
                        userFamilyMemberFragment.initLayout()
                        stepperLayout.setStepperButtonEnabled(true)
                    }

                    is UserSelfieInputFragment -> {
                        stepperLayout.setSubmitButtonText(resources.getString(R.string.register))
                        userSelfieInputFragment.validateSection()
                    }
                }
            }
        }

        userCredentialInputFragment = UserCredentialInputFragment.newInstance()
        userInformationInputFragment = UserInformationInputFragment.newInstance()
        userAddressInputFragment = UserAddressInputFragment.newInstance()
        userEducationInputFragment = UserEducationInputFragment.newInstance()
        userBaptismInputFragment = UserBaptismInputFragment.newInstance()
        userMartialInputFragment = UserMartialInputFragment.newInstance()
        userFamilyMemberFragment = UserFamilyMemberInputFragment.newInstance()
        userSelfieInputFragment = UserSelfieInputFragment.newInstance()

        stepperAdapter = HancinStepperAdapter(supportFragmentManager)
        stepperAdapter.put(userCredentialInputFragment)
        stepperAdapter.put(userInformationInputFragment)
        stepperAdapter.put(userAddressInputFragment)
        stepperAdapter.put(userEducationInputFragment)
        stepperAdapter.put(userBaptismInputFragment)
        stepperAdapter.put(userMartialInputFragment)
        stepperAdapter.put(userFamilyMemberFragment)
        stepperAdapter.put(userSelfieInputFragment)

        stepperLayout.adapter = stepperAdapter
    }

    fun enableStepperButton(enable: Boolean) {
        stepperLayout.setStepperButtonEnabled(enable)
    }

    private fun showLoading(isVisible: Boolean) {
        if (isVisible) binding.loading.visibility = View.VISIBLE
        else binding.loading.visibility = View.GONE
    }

    override fun onBackPressed() {
        val isPreviousSectionAvailable: Boolean = stepperAdapter.prev(stepperLayout)
        if (!isPreviousSectionAvailable) {
            UIHelper.getInstance().displayConfirmation(
                resources.getString(R.string.dialog_general_cancel),
                "",
                this,
                { },
                {
                    finish()
                },
                resources.getString(R.string.text_confirmation_no),
                resources.getString(R.string.text_confirmation_yes_),
                R.drawable.question,
                true
            )
        }
    }

    private fun showSuccessDialog() {
        binding.loading.visibility = View.GONE
        UIHelper.getInstance().displaySuccessDialog(
            resources.getString(R.string.success_user_registration_title),
            resources.getString(R.string.success_user_registration_desc),
            this,
            {
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
            },
            resources.getString(R.string.OK),
            false,
        )
    }

    private fun observeViewModel() {
        viewModel.isSectionValid.observe(this) {
            enableStepperButton(it)
        }
        viewModel.isEnableToSubmit.observe(this) {
            if (it) {
                stepperLayout.setStepperButtonEnabled(false)
                stepperLayout.setSubmitButtonEnabled(true)
            } else {
                stepperLayout.setSubmitButtonEnabled(false)
            }
        }
        viewModel.isSuccessCreateNewUser.observe(this) {
            showSuccessDialog()
        }
        viewModel.isFailCreateNewUser.observe(this) { errorMessage ->
            showLoading(false)
            Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
        }
        viewModel.isUserAlreadyRegistered.observe(this) {
            showLoading(false)
            Toast.makeText(applicationContext, "Email sudah terdaftar", Toast.LENGTH_LONG).show()
        }
    }
}