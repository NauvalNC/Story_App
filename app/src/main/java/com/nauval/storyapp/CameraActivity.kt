package com.nauval.storyapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.nauval.storyapp.databinding.ActivityCameraBinding
import com.nauval.storyapp.helper.IGeneralSetup
import com.nauval.storyapp.helper.Utils

class CameraActivity : AppCompatActivity(), IGeneralSetup {

    private lateinit var binding: ActivityCameraBinding
    private var camSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var imgCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setup()
    }

    override fun setup() {
        binding.apply {
            captureCam.setOnClickListener { captureImage() }
            switchCam.setOnClickListener {
                camSelector = if (camSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
                else CameraSelector.DEFAULT_BACK_CAMERA
                openCamera()
            }
        }
    }

    override fun enableControl(isEnabled: Boolean) {
        binding.apply {
            captureCam.isEnabled = isEnabled
            switchCam.isEnabled = isEnabled
            loadingBar.visibility = if (isEnabled) View.GONE else View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        openCamera()
        hideDefaultSystemUI()
    }

    private fun openCamera() {
        val futureProvider = ProcessCameraProvider.getInstance(this@CameraActivity)
        futureProvider.addListener({
            imgCapture = ImageCapture.Builder().build()

            val provider = futureProvider.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(binding.camViewFinder.surfaceProvider) }

            try {
                provider.apply {
                    unbindAll()
                    bindToLifecycle(this@CameraActivity, camSelector, preview, imgCapture) }
            } catch (exc: Exception) {
                Toast.makeText(
                    this@CameraActivity,
                    resources.getString(R.string.camera_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, ContextCompat.getMainExecutor(this@CameraActivity))
    }

    private fun captureImage() {
        enableControl(false)

        val toCapture = imgCapture ?: return
        val file = Utils.makeTempFile(application)
        val outputOption = ImageCapture.OutputFileOptions.Builder(file).build()

        toCapture.takePicture(outputOption,
            ContextCompat.getMainExecutor(this@CameraActivity),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    enableControl(true)

                    Toast.makeText(
                        this@CameraActivity, R.string.camera_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    setResult(AddNewStoryActivity.CAMERA_RESULT, Intent().apply {
                        putExtra(CAPTURED_IMG, file)
                        putExtra(IS_BACK_CAM, camSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                    })
                    finish()
                }
            }
        )
    }

    private fun hideDefaultSystemUI() {
        supportActionBar?.hide()

        @Suppress("DEPRECATION")
        window.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                insetsController?.hide(WindowInsets.Type.statusBars())
            else
                setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    companion object {
        const val CAPTURED_IMG = "CAPTURED_IMG"
        const val IS_BACK_CAM = "IS_BACK_CAMERA"
    }
}