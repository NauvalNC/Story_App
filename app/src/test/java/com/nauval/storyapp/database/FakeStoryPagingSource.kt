package com.nauval.storyapp.database

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nauval.storyapp.DummyData
import com.nauval.storyapp.helper.StoryItemResponse

class FakeStoryPagingSource: PagingSource<Int, StoryItemResponse>() {
    var stories: List<StoryItemResponse> = DummyData.generateDummyStories()
    var isError = false

    override fun getRefreshKey(state: PagingState<Int, StoryItemResponse>): Int =
        state.anchorPosition ?: 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryItemResponse> {
        if (isError) return LoadResult.Error(Exception("Error test triggered"))
        return LoadResult.Page(data = stories, prevKey = null, nextKey = null)
    }
}