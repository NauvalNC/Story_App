package com.nauval.storyapp.api

import com.nauval.storyapp.helper.StoryResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface StoryPagingApiService {
    @GET("stories")
    suspend fun getStoryPages(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): StoryResponse
}