package com.nauval.storyapp.repository

import com.nauval.storyapp.api.StoryBusinessApiService
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryBusinessRepository (private val apiService: StoryBusinessApiService) {

    suspend fun getStoriesWithLocation(token: String) =
        apiService.getStoriesWithLocation("Bearer $token", 1)

    suspend fun uploadStory(
        token: String,
        file: MultipartBody.Part,
        desc: RequestBody,
        lat: RequestBody,
        lon: RequestBody
    ) = apiService.uploadStory("Bearer $token", file, desc, lat, lon)
}