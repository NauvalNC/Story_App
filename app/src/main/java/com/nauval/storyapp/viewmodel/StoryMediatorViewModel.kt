package com.nauval.storyapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nauval.storyapp.helper.StoryItemResponse
import com.nauval.storyapp.repository.StoryRepository

class StoryMediatorViewModel(private val repository: StoryRepository): ViewModel() {
    fun storyList(token: String): LiveData<PagingData<StoryItemResponse>> = repository.getStories(token).cachedIn(viewModelScope)
}