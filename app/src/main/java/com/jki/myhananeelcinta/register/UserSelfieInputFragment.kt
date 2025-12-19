package com.jki.myhananeelcinta.register

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jki.myhananeelcinta.R
import com.jki.myhananeelcinta.camera.CameraActivity
import com.jki.myhananeelcinta.databinding.FragmentUserSelfieInformationInputBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class UserSelfieInputFragment : Fragment() {

    private lateinit var binding: FragmentUserSelfieInformationInputBinding
    private lateinit var viewModel: RegisterViewModel
    private var isPhotoCaptured: Boolean = false

    companion object {
        @JvmStatic
        fun newInstance() = UserSelfieInputFragment().apply { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.let { ViewModelProvider(it)[RegisterViewModel::class.java] }!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_user_selfie_information_input,
            container,
            false
        )

        binding.btnTakePhoto.setOnClickListener {
            val intent = Intent(context, CameraActivity::class.java)
            getResult.launch(intent)

        }
        return binding.root
    }

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val capturedImageData =
                    result.data?.getByteArrayExtra(CameraActivity.ARG_CAMERA_RESULT)
                capturedImageData?.let { imageData ->
                    val capturedImage = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                    viewModel.capturedImageFile =
                        getProfilePictureFile(
                            "profile-picture.jpg",
                            capturedImage
                        )
                    isPhotoCaptured = true
                    binding.ivSelfie.setImageBitmap(capturedImage)
                    validateSection()
                }
            }
        }

    private fun getProfilePictureFile(fileName: String, bitmap: Bitmap): File? {
        val file = File(requireContext().cacheDir, fileName)
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

    fun validateSection() {
        viewModel.setIsEnableToSubmit(
            isPhotoCaptured
        )
    }
}