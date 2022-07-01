package com.nauval.storyapp.api

import com.nauval.storyapp.DummyData
import com.nauval.storyapp.helper.StoryResponse
import com.nauval.storyapp.helper.StoryUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class FakeStoryBusinessApiService: StoryBusinessApiService {
    var dummyUploadResponse: Response<StoryUploadResponse> =
        DummyData.getDummyUploadStoryResponse_Success()

    var dummyStoryResponse: Response<StoryResponse> =
        DummyData.getDummyStoryResponse_Success()

    override suspend fun getStoriesWithLocation(
        token: String,
        location: Int
    ): Response<StoryResponse> {
        return dummyStoryResponse
    }

    override suspend fun uploadStory(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody,
        lon: RequestBody
    ): Response<StoryUploadResponse> {
        return dummyUploadResponse
    }
}