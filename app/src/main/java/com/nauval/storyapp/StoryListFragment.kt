package com.nauval.storyapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nauval.storyapp.adapter.GeneralLoadingStateAdapter
import com.nauval.storyapp.adapter.ListStoryPagingAdapter
import com.nauval.storyapp.databinding.FragmentStoryListBinding
import com.nauval.storyapp.factory.StoryMediatorViewModelFactory
import com.nauval.storyapp.helper.StoryApiConfig
import com.nauval.storyapp.helper.StoryApiSession
import com.nauval.storyapp.helper.StoryItemResponse
import com.nauval.storyapp.viewmodel.StoryMediatorViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class StoryListFragment : Fragment() {
    private lateinit var binding: FragmentStoryListBinding
    private lateinit var storyMediatorVm: StoryMediatorViewModel
    private lateinit var storyRvAdapter: ListStoryPagingAdapter

    private val launcherForNewStory = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == MainLandingPageActivity.NEW_STORY_ADDED_RESULT) {
            refreshList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStoryListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        storyRvAdapter = ListStoryPagingAdapter().apply {
            setOnItemClickCallback(object : ListStoryPagingAdapter.OnItemClickCallback {
                override fun onItemClicked(story: StoryItemResponse, position: Int) {
                    requireContext().startActivity(
                        Intent(
                            requireContext(),
                            StoryDetailsWithMapActivity::class.java
                        ).apply {
                            putExtra(StoryApiConfig.STORY_EXTRA, story)
                        })
                }
            })
        }

        storyMediatorVm = ViewModelProvider(
            requireActivity(),
            StoryMediatorViewModelFactory(requireContext())
        )[StoryMediatorViewModel::class.java]

        lifecycleScope.launch {
            storyRvAdapter.loadStateFlow.collect { loadingState ->
                val isListEmpty = storyRvAdapter.itemCount == 0
                val isError = loadingState.refresh is LoadState.Error
                val isSourceLoading = loadingState.source.refresh is LoadState.Loading

                if (isListEmpty && !isSourceLoading && !isError) refreshList()

                binding.storyRv.isVisible = !isListEmpty
                binding.noData.isVisible = isError && !binding.storyRv.isVisible
                binding.swipeRefresh.isRefreshing = (isSourceLoading && isListEmpty)
            }
        }
        val token = StoryApiSession(requireContext()).getToken()
        storyMediatorVm.storyList(token).observe(viewLifecycleOwner) {
            storyRvAdapter.submitData(lifecycle, it)
        }

        binding.apply {
            newStoryBtn.setOnClickListener {
                launcherForNewStory.launch(Intent(requireContext(), AddNewStoryActivity::class.java))
            }

            swipeRefresh.setOnRefreshListener {
                swipeRefresh.isRefreshing = true
                refreshList()
            }

            storyRv.layoutManager = LinearLayoutManager(requireActivity())
            storyRv.adapter = storyRvAdapter.withLoadStateFooter(footer = GeneralLoadingStateAdapter {
                storyRvAdapter.retry()
            })
        }

        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)?.setOnItemReselectedListener {
            refreshList()
        }
    }

    private fun refreshList() {
        storyRvAdapter.refresh()
        binding.storyRv.smoothScrollToPosition(binding.storyRv.top)
    }
}