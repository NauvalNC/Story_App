package com.nauval.storyapp

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.nauval.storyapp.databinding.ActivityStoryDetailsWithMapBinding
import com.nauval.storyapp.helper.IGeneralSetup
import com.nauval.storyapp.helper.StoryApiConfig
import com.nauval.storyapp.helper.StoryItemResponse
import com.nauval.storyapp.helper.Utils
import com.nauval.storyapp.helper.Utils.formatDate

class StoryDetailsWithMapActivity : AppCompatActivity(), OnMapReadyCallback, IGeneralSetup {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityStoryDetailsWithMapBinding
    private var story: StoryItemResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoryDetailsWithMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.story_details)

        (supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)
    }

    override fun setup() {
        story = intent.getParcelableExtra(StoryApiConfig.STORY_EXTRA)

        val username = story?.name
        val date = story?.createdAt?.formatDate()
        val lat = story?.lat
        val lon = story?.lon

        binding.apply {
            postUsername.text = username
            postDescription.text = story?.description
            postDate.text = date
            Glide.with(this@StoryDetailsWithMapActivity)
                .load(story?.photoUrl)
                .thumbnail(0.5f)
                .override( 256, 256)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(Utils.getCircularProgressDrawable(this@StoryDetailsWithMapActivity))
                .into(binding.postImage)
        }

        if (lat != null && lon != null) {
            val loc = LatLng(lat, lon)
            mMap.addMarker(
                MarkerOptions()
                    .position(loc)
                    .title(username)
                    .snippet(date)
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f))
            binding.resetBtn.setOnClickListener { mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f)) }
        } else {
            binding.resetBtn.visibility = View.GONE
            binding.noData.visibility = View.VISIBLE
            Toast.makeText(this@StoryDetailsWithMapActivity, resources.getString(R.string.no_location), Toast.LENGTH_SHORT).show()
        }
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        Utils.setMapTheme(this, mMap)

        setup()
    }


}