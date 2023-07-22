package com.jki.hananeelcinta.camera

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityCameraBinding
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Grid
import com.otaliastudios.cameraview.controls.Mode
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var cameraView: CameraView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        supportActionBar?.hide()

        cameraView.setLifecycleOwner(this)
        cameraView.mode = Mode.PICTURE
        cameraView.snapshotMaxHeight = 500
        cameraView.snapshotMaxWidth = 500
        cameraView.grid = Grid.DRAW_3X3
        cameraView.gridColor = Color.WHITE
        cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)

                val imageData = result.data

                val intent = Intent()
                intent.putExtra(ARG_CAMERA_RESULT, imageData)
                setResult(RESULT_OK, intent)
                finish()
            }
        })

        //setup listener for take photo
        binding.btnImageCapture.setOnClickListener {
            cameraView.takePicture()
        }
    }

    companion object {
        const val RESULT_IMAGE_PATH = "image_path"
        private const val TAG = "CameraXApp"
        private const val FILE_NAME_FORMAT = "yyyy-MM-dd_HH-mm_ss_SSS"
        private const val REQUEST_CODE_PERMISSION = 10
        private val REQUIRED_PERMISSION = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
        const val ARG_CAMERA_RESULT = "argCameraResult"
    }
}