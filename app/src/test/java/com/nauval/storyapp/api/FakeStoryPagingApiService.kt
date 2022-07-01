package com.nauval.storyapp.api

import com.nauval.storyapp.DummyData
import com.nauval.storyapp.helper.StoryResponse

class FakeStoryPagingApiService: StoryPagingApiService {
    override suspend fun getStoryPages(token: String, page: Int, size: Int): StoryResponse {
        return DummyData.generateStory()
    }
}