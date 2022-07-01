package com.nauval.storyapp.database

import androidx.paging.PagingSource
import com.nauval.storyapp.helper.StoryItemResponse

class FakeStoryDao: StoryDao {
    private val storySource = FakeStoryPagingSource()

    override suspend fun insertStories(stories: List<StoryItemResponse>) { storySource.stories = stories }

    override fun getAllStory(): PagingSource<Int, StoryItemResponse> { return storySource }

    override suspend fun deleteAllStories() { storySource.stories = emptyList() }
}