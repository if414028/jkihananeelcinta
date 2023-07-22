package com.jki.hananeelcinta.register

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.camera.CameraActivity
import com.jki.hananeelcinta.databinding.FragmentUserSelfieInformationInputBinding
import java.io.File


class UserSelfieInputFragment : Fragment() {

    private lateinit var binding: FragmentUserSelfieInformationInputBinding
    private var capturedBitmap: Bitmap? = null

    companion object {
        @JvmStatic
        fun newInstance() = UserSelfieInputFragment().apply { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    capturedBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                    binding.ivSelfie.setImageBitmap(capturedBitmap)
                }
            }
        }

    private fun setCapturedImage(selectedFileUri: Uri) {
        val fixImageresult = getRealPathFromURI(selectedFileUri)
        val imageFile = File(fixImageresult)
        if (imageFile.exists()) {
            val imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            binding.ivSelfie.setImageBitmap(imageBitmap)
        }
    }

    fun getRealPathFromURI(uri: Uri?): String? {
        var path = ""
        if (context?.contentResolver != null) {
            val cursor: Cursor? =
                uri?.let { requireContext().contentResolver.query(it, null, null, null, null) }
            if (cursor != null) {
                cursor.moveToFirst()
                val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }
}