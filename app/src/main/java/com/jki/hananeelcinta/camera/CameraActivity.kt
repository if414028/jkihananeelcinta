package com.jki.hananeelcinta.camera

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.jki.hananeelcinta.R
import com.jki.hananeelcinta.databinding.ActivityCameraBinding
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Grid
import com.otaliastudios.cameraview.controls.Mode

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var cameraView: CameraView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        supportActionBar?.hide()

        cameraView = binding.camera
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
            cameraView.takePictureSnapshot()
        }
    }

    companion object {
        const val ARG_CAMERA_RESULT = "argCameraResult"
    }
}