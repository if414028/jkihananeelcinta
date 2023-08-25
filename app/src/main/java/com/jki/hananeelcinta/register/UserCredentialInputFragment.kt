package com.jki.hananeelcinta.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.FragmentUserCredentialInputBinding

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
        var isEmailValid = false
        var isUsernameValid = false
        var isPasswordValid = false
        var isRePasswordValid = false
        binding.etEmail.addTextChangedListener {
            isEmailValid = it.toString().isNotEmpty()
            if (isEmailValid)
                binding.tvEmailErrorMessage.visibility = View.GONE
            else binding.tvEmailErrorMessage.visibility = View.VISIBLE
            viewModel.setIsSectionValid((isEmailValid && isUsernameValid && isPasswordValid && isRePasswordValid))
        }
        binding.etUsername.addTextChangedListener {
            isUsernameValid = it.toString().isNotEmpty()
            if (isUsernameValid)
                binding.tvUsernameErrorMessage.visibility = View.GONE
            else binding.tvUsernameErrorMessage.visibility = View.VISIBLE
            viewModel.setIsSectionValid((isEmailValid && isUsernameValid && isPasswordValid && isRePasswordValid))
        }
        binding.etPassword.addTextChangedListener {
            isPasswordValid = it.toString().isNotEmpty()
            if (isPasswordValid)
                binding.tvPasswordErrorMessage.visibility = View.GONE
            else binding.tvPasswordErrorMessage.visibility = View.VISIBLE
            viewModel.setIsSectionValid((isEmailValid && isUsernameValid && isPasswordValid && isRePasswordValid))
        }
        binding.etPasswordConfirmation.addTextChangedListener {
            isRePasswordValid = it.toString() == binding.etPassword.text.toString()
            if (isRePasswordValid)
                binding.tvPasswordConfirmationErrorMessage.visibility = View.GONE
            else binding.tvPasswordConfirmationErrorMessage.visibility = View.VISIBLE
            viewModel.setIsSectionValid((isEmailValid && isUsernameValid && isPasswordValid && isRePasswordValid))
        }
    }

    fun validateSection() {
        viewModel.setIsSectionValid(
            binding.etEmail.text!!.isNotEmpty()
                    && binding.etUsername.text!!.isNotEmpty()
                    && binding.etPassword.text!!.isNotEmpty()
                    && binding.etPasswordConfirmation.text.toString() == binding.etPassword.text.toString()
        )
    }
}