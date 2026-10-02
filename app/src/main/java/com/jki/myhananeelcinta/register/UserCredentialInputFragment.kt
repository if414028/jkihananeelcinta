package com.jki.myhananeelcinta.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jki.myhananeelcinta.R
import com.jki.myhananeelcinta.databinding.FragmentUserCredentialInputBinding

class UserCredentialInputFragment : Fragment() {

    private lateinit var binding: FragmentUserCredentialInputBinding
    private lateinit var viewModel: RegisterViewModel

    companion object {
        @JvmStatic
        fun newInstance() = UserCredentialInputFragment().apply {

        }
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
            R.layout.fragment_user_credential_input,
            container,
            false
        )

        validateEachField()

        return binding.root
    }

    fun setUserCredential() {
        viewModel.setUserCredential(
            binding.etUsername.text.toString(),
            binding.etEmail.text.toString(),
            binding.etPassword.text.toString(),
            binding.etPasswordConfirmation.text.toString()
        )
    }

    private fun validateEachField() {
        listOf(binding.etEmail, binding.etUsername, binding.etPassword, binding.etPasswordConfirmation)
            .forEach { field -> field.addTextChangedListener { validateSection() } }
    }

    fun validateSection() {
        val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString().trim()).matches()
        val nicknameValid = binding.etUsername.text.toString().trim().length <= 100
        val passwordValid = binding.etPassword.text.toString().length >= 6
        val confirmationValid = binding.etPasswordConfirmation.text.toString() == binding.etPassword.text.toString()
        binding.tvEmailErrorMessage.visibility = if (emailValid) View.GONE else View.VISIBLE
        binding.tvUsernameErrorMessage.visibility = if (nicknameValid) View.GONE else View.VISIBLE
        binding.tvPasswordErrorMessage.visibility = if (passwordValid) View.GONE else View.VISIBLE
        binding.tvPasswordConfirmationErrorMessage.visibility = if (confirmationValid) View.GONE else View.VISIBLE
        viewModel.setIsSectionValid(emailValid && nicknameValid && passwordValid && confirmationValid)
    }
}
