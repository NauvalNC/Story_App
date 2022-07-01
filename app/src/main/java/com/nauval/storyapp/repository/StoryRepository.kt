package com.nauval.storyapp.repository

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.nauval.storyapp.api.StoryPagingApiService
import com.nauval.storyapp.data.StoryRemoteMediator
import com.nauval.storyapp.database.StoryDatabase
import com.nauval.storyapp.helper.StoryApiConfig
import com.nauval.storyapp.helper.StoryItemResponse

class StoryRepository(
    private val database: StoryDatabase,
    private val apiService: StoryPagingApiService
) {
    @OptIn(ExperimentalPagingApi::class)
    fun getStories(token: String): LiveData<PagingData<StoryItemResponse>> {
        return Pager(
            config = PagingConfig(pageSize = StoryApiConfig.MIN_PAGE),
            remoteMediator = StoryRemoteMediator(token, database, apiService),
            pagingSourceFactory = { database.storyDao().getAllStory() }
        ).liveData
    }
}