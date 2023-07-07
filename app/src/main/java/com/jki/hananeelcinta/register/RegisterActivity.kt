package com.jki.hananeelcinta.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.jki.hananeelcinta.home.MainActivity
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityRegisterBinding
import com.jki.hananeelcinta.stepper.HancinStepperAdapter
import com.jki.hananeelcinta.stepper.HancinStepperLayout
import com.jki.hananeelcinta.stepper.contract.HancinStepperContract
import com.jki.hananeelcinta.util.UIHelper

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
    private lateinit var userSelfieInputFragment: UserSelfieInputFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)
        supportActionBar?.hide()

        setupStepperAdapter()
        observeViewModel()
    }

    private fun setupStepperAdapter() {
        stepperLayout = binding.stepperContainer
        stepperLayout.listener = object : HancinStepperContract.Event {
            override fun onSubmitClicked() {
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
                        stepperLayout.setStepperButtonEnabled(true)
                    }

                    is UserMartialInputFragment -> {
                        stepperLayout.setStepperButtonEnabled(true)
                    }

                    is UserSelfieInputFragment -> {
                        stepperLayout.setSubmitButtonText(resources.getString(R.string.register))
                        stepperLayout.setStepperButtonEnabled(false)
                        stepperLayout.setSubmitButtonEnabled(true)
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
        userSelfieInputFragment = UserSelfieInputFragment.newInstance()

        stepperAdapter = HancinStepperAdapter(supportFragmentManager)
        stepperAdapter.put(userCredentialInputFragment)
        stepperAdapter.put(userInformationInputFragment)
        stepperAdapter.put(userAddressInputFragment)
        stepperAdapter.put(userEducationInputFragment)
        stepperAdapter.put(userBaptismInputFragment)
        stepperAdapter.put(userMartialInputFragment)
        stepperAdapter.put(userSelfieInputFragment)

        stepperLayout.adapter = stepperAdapter
    }

    fun enableStepperButton(enable: Boolean) {
        stepperLayout.setStepperButtonEnabled(enable)
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

    private fun observeViewModel() {
        viewModel.isSectionValid.observe(this) {
            enableStepperButton(it)
        }
        viewModel.isSuccessCreateNewUser.observe(this) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
        viewModel.isFailCreateNewUser.observe(this) { errorMessage ->
            Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}