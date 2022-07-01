package com.nauval.storyapp.api

import com.nauval.storyapp.helper.StoryResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface StoryApiService {
    @GET("stories")
    fun getStoriesSync(@Header("Authorization") token: String): Call<StoryResponse>
}