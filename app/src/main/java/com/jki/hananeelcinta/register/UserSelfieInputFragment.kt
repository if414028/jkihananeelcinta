package com.jki.hananeelcinta.register

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.database.Cursor
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
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val value = it.data?.getStringExtra(CameraActivity.RESULT_IMAGE_PATH)
                val imageUri = Uri.parse(value)
                setCapturedImage(imageUri)
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