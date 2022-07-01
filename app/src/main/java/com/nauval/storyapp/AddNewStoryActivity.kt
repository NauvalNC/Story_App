package com.nauval.storyapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.nauval.storyapp.databinding.ActivityAddNewStoryBinding
import com.nauval.storyapp.factory.StoryBusinessViewModelFactory
import com.nauval.storyapp.helper.*
import com.nauval.storyapp.repository.StoryBusinessRepository
import com.nauval.storyapp.viewmodel.StoryBusinessViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AddNewStoryActivity : AppCompatActivity(), IGeneralSetup {

    private lateinit var binding: ActivityAddNewStoryBinding
    private lateinit var storyBusinessVm: StoryBusinessViewModel

    private var imageFile: File? = null
    private val compressExecutor = Executors.newFixedThreadPool(1)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private var updatedLoc: Location? = null
    private lateinit var locationRequest: LocationRequest
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                updatedLoc = location
                break
            }
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val chosenImage: Uri = result.data?.data as Uri
            setPlaceholderImage(chosenImage, null)
            imageFile = Utils.convertUriToFile(chosenImage, this@AddNewStoryActivity)
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == CAMERA_RESULT) {
            val file = result.data?.getSerializableExtra(CameraActivity.CAPTURED_IMG) as File
            val isBackCamera = result.data?.getBooleanExtra(CameraActivity.IS_BACK_CAM, true) as Boolean
            val bitmap = Utils.stabilizeRotateBitmap(BitmapFactory.decodeFile(file.path), isBackCamera)
            setPlaceholderImage(null, bitmap)
            imageFile = Utils.transferBitmapToFile(bitmap, file)
        }
    }

    private val launcherGpsSetting = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            RESULT_OK -> { startLocationUpdates() }
            RESULT_CANCELED -> {
                Toast.makeText(
                    this@AddNewStoryActivity,
                    resources.getString(R.string.location_failure),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private val imageCompressListener: Utils.CompressImageTask.ICompressListener =
        object : Utils.CompressImageTask.ICompressListener {
            override fun onComplete(compressedFile: File) {
                beginUpload(compressedFile)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.add_new_story)

        storyBusinessVm = ViewModelProvider(
            this@AddNewStoryActivity,
            StoryBusinessViewModelFactory(StoryBusinessRepository(StoryApiConfig.getStoryBusinessApiService()))
        )[StoryBusinessViewModel::class.java]

        storyBusinessVm.apply {
            uploadResponse.observe(this@AddNewStoryActivity) {
                Toast.makeText(
                    this@AddNewStoryActivity,
                    resources.getString(R.string.upload_success),
                    Toast.LENGTH_SHORT
                ).show()
                onSuccessUpload()
            }
            isError.observe(this@AddNewStoryActivity) {
                EspressoIdlingResource.decrement()
                if (it) {
                    Toast.makeText(
                        this@AddNewStoryActivity,
                        resources.getString(R.string.upload_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                    enableControl(true)
                }
            }
        }

        setup()
    }

    override fun onResume() {
        super.onResume()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (!isGPSEnabled()) requestGPS()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //region Setup
    override fun setup() {
        if (!isAllPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this, NEW_STORY_PERMISSIONS, NEW_STORY_PERMISSION_RESULT_CODE)
        }

        binding.apply {
            cameraBtn.setOnClickListener { openCamera() }
            galleryBtn.setOnClickListener { openGallery() }
            uploadBtn.setOnClickListener { uploadStory() }
        }
    }

    override fun isFieldVerified(): Boolean {
        if (imageFile == null) {
            Toast.makeText(
                this@AddNewStoryActivity,
                resources.getString(R.string.upload_image_null),
                Toast.LENGTH_SHORT
            ).show()
        }

        return (binding.descriptionField.isValid(withToast = true) && imageFile != null)
    }

    override fun enableControl(isEnabled: Boolean) {
        binding.apply {
            uploadBtn.isEnabled = isEnabled
            cameraBtn.isEnabled = isEnabled
            galleryBtn.isEnabled = isEnabled
            loadingBar.visibility = if (isEnabled) View.GONE else View.VISIBLE
        }
    }

    private fun setPlaceholderImage(chosenUri: Uri?, chosenBitmap: Bitmap?) {
        binding.apply {
            if (chosenUri != null) previewImage.setImageURI(chosenUri)
            if (chosenBitmap != null) previewImage.setImageBitmap(chosenBitmap)
            imagePlaceholder.visibility = View.GONE
        }
    }
    //endregion

    //region Camera and Gallery Methods
    private fun openGallery() {
        launcherIntentGallery.launch(
            Intent.createChooser(
                Intent().apply {
                    action = ACTION_GET_CONTENT
                    type = "image/*"
                }, resources.getString(R.string.choose_image)
            )
        )
    }

    private fun openCamera() {
        launcherIntentCamera.launch(Intent(this, CameraActivity::class.java))
    }
    //endregion

    //region Upload Methods
    private fun uploadStory() {
        when {
            !isGPSEnabled() -> requestGPS()
            isFieldVerified() -> {
                enableControl(false)
                requestCurrentLocation()
            }
        }
    }

    private fun beginUpload(file: File) {
        val desc = binding.descriptionField.text.toString().toRequestBody("text/plain".toMediaType())
        val lon = updatedLoc?.longitude.toString().toRequestBody("text/plain".toMediaType())
        val lat = updatedLoc?.latitude.toString().toRequestBody("text/plain".toMediaType())

        val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())

        val storyMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            file.name,
            requestImageFile
        )

        EspressoIdlingResource.increment()
        storyBusinessVm.uploadStory(
            StoryApiSession(this@AddNewStoryActivity).getToken(),
            storyMultipart, desc, lat, lon
        )
    }

    private fun onSuccessUpload() {
        setResult(MainLandingPageActivity.NEW_STORY_ADDED_RESULT, Intent())
        finish()
    }
    //endregion

    //region Location and GPS
    private fun requestGPS() {
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(1)
            maxWaitTime = TimeUnit.SECONDS.toMillis(1)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        LocationServices.getSettingsClient(this)
            .checkLocationSettings(LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build())
            .addOnSuccessListener { startLocationUpdates() }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        launcherGpsSetting.launch(IntentSenderRequest.Builder(exception.resolution).build())
                    } catch (exception: IntentSender.SendIntentException) {
                        Toast.makeText(
                            this@AddNewStoryActivity,
                            resources.getString(R.string.location_failure),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    private fun requestCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            enableControl(true)
            Toast.makeText(this@AddNewStoryActivity, resources.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    updatedLoc = location
                    compressExecutor.execute(Utils.CompressImageTask(imageFile as File, imageCompressListener))
                }
                else {
                    enableControl(true)
                    Toast.makeText(this@AddNewStoryActivity, resources.getString(R.string.location_failure), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (exception: SecurityException) {
            Toast.makeText(this@AddNewStoryActivity, resources.getString(R.string.location_failure), Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    //endregion

    //region Permissions
    private fun isGPSEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NEW_STORY_PERMISSION_RESULT_CODE) {
            if (!isAllPermissionGranted()) {
                Toast.makeText(
                    this, resources.getString(R.string.permission_denied), Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun isAllPermissionGranted() = NEW_STORY_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    //endregion

    companion object {
        const val CAMERA_RESULT = 120
        private const val NEW_STORY_PERMISSION_RESULT_CODE = 10
        private val NEW_STORY_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
    }
}