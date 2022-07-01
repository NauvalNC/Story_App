package com.nauval.storyapp

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.nauval.storyapp.databinding.FragmentStoryMapListBinding
import com.nauval.storyapp.factory.StoryBusinessViewModelFactory
import com.nauval.storyapp.helper.*
import com.nauval.storyapp.helper.Utils.formatDate
import com.nauval.storyapp.repository.StoryBusinessRepository
import com.nauval.storyapp.viewmodel.StoryBusinessViewModel

class StoryMapListFragment : Fragment(), OnMapReadyCallback, IGeneralSetup {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentStoryMapListBinding
    private lateinit var storyBusinessVm: StoryBusinessViewModel

    private var boundsBuilder = LatLngBounds.Builder()
    private var stories: MutableMap<String, StoryItemResponse> = mutableMapOf()
    private var defaultViewDetailY = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStoryMapListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultViewDetailY = binding.storyDetail.translationY
        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isIndoorLevelPickerEnabled = false
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.setOnMapClickListener { hideViewDetails() }
        mMap.setOnMarkerClickListener {
            stories[it.id]?.let { story ->
                    binding.apply {
                        postUsername.text = story.name
                        postDate.text = story.createdAt.formatDate()

                        Glide.with(requireContext())
                            .load(story.photoUrl)
                            .thumbnail(0.5f)
                            .override( 128, 128)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(Utils.getCircularProgressDrawable(requireContext()))
                            .into(binding.postImage)

                        viewDetailBtn.setOnClickListener {
                            requireContext().startActivity(Intent(requireContext(), StoryDetailsWithMapActivity::class.java).apply {
                                putExtra(StoryApiConfig.STORY_EXTRA, story)
                            })
                        }

                        showViewDetails()
                    }
            }
            false
        }

        Utils.setMapTheme(requireContext(), mMap)

        setup()
    }

    override fun setup() {
        storyBusinessVm = ViewModelProvider(
            requireActivity(),
            StoryBusinessViewModelFactory(StoryBusinessRepository(StoryApiConfig.getStoryBusinessApiService()))
        )[StoryBusinessViewModel::class.java]

        storyBusinessVm.apply {
            storiesWithLocation.observe(viewLifecycleOwner) {
                clearMaps()
                addMarkers(it)
            }
            isError.observe(viewLifecycleOwner) {
                EspressoIdlingResource.decrement()
                if (it) {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.load_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                binding.loadingBar.visibility = View.GONE
            }
        }

        binding.refreshBtn.setOnClickListener { refreshStory() }
        refreshStory()
    }

    private fun clearMaps() {
        mMap.clear()
        stories.clear()
        boundsBuilder = LatLngBounds.Builder()
    }

    private fun addMarkers(listStory: List<StoryItemResponse>) {
        for(story in listStory) {
            if (story.lat == null || story.lon == null) continue

            val loc = LatLng(story.lat, story.lon)
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(loc)
                    .title(story.name)
                    .snippet(story.createdAt.formatDate())
            )

            if (marker != null) {
                stories[marker.id] = story
                boundsBuilder.include(loc)
            }
        }

        viewBound()
    }

    private fun refreshStory() {
        EspressoIdlingResource.increment()
        binding.loadingBar.visibility = View.VISIBLE
        storyBusinessVm.getStoriesWithLocation(StoryApiSession(requireContext()).getToken())
    }

    private fun viewBound() {
        if (stories.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.no_marker), Toast.LENGTH_SHORT).show()
            return
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 64))
    }

    private fun showViewDetails() {
        binding.viewDetailBtn.isEnabled = true
        ObjectAnimator.ofFloat(binding.storyDetail, View.TRANSLATION_Y, 0f).setDuration(IGeneralSetup.ANIM_DURATION).start()
    }

    private fun hideViewDetails() {
        binding.viewDetailBtn.isEnabled = false
        ObjectAnimator.ofFloat(binding.storyDetail, View.TRANSLATION_Y, defaultViewDetailY).setDuration(IGeneralSetup.ANIM_DURATION).start()
    }
}