package com.nauval.storyapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nauval.storyapp.helper.StoryItemResponse
import com.nauval.storyapp.helper.StoryUploadResponse
import com.nauval.storyapp.repository.StoryBusinessRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryBusinessViewModel (private val repository: StoryBusinessRepository): ViewModel() {
    private var _storiesWithLocation = MutableLiveData<List<StoryItemResponse>>()
    val storiesWithLocation: LiveData<List<StoryItemResponse>> = _storiesWithLocation

    private var _uploadResponse = MutableLiveData<StoryUploadResponse>()
    val uploadResponse: LiveData<StoryUploadResponse> = _uploadResponse

    private var _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    fun getStoriesWithLocation(token: String) = viewModelScope.launch {
        try {
            val response = repository.getStoriesWithLocation(token)
            if (response.isSuccessful) {
                _storiesWithLocation.postValue(response.body()!!.stories)
            }
            _isError.postValue(!response.isSuccessful)
        } catch (e: Exception) {
            _isError.postValue(true)
            e.printStackTrace()
        }
    }

    fun uploadStory(
        token: String,
        file: MultipartBody.Part,
        desc: RequestBody,
        lat: RequestBody,
        lon: RequestBody
    ) = viewModelScope.launch {
        try {
            val response = repository.uploadStory(token, file, desc, lat, lon)
            if (response.isSuccessful) {
                _uploadResponse.postValue(response.body()!!)
            }
            _isError.postValue(!response.isSuccessful)
        } catch (e: Exception) {
            _isError.postValue(true)
            e.printStackTrace()
        }
    }
}