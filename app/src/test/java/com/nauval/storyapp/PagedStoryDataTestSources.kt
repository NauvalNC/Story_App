package com.nauval.storyapp

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nauval.storyapp.helper.StoryItemResponse

class PagedStoryDataTestSources private constructor(private val items: List<StoryItemResponse>) :
    PagingSource<Int, LiveData<List<StoryItemResponse>>>() {

    override fun getRefreshKey(state: PagingState<Int, LiveData<List<StoryItemResponse>>>): Int = 0

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<StoryItemResponse>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }

    companion object {
        fun getSnapshot(items: List<StoryItemResponse>): PagingData<StoryItemResponse> {
            return PagingData.from(items)
        }
    }
}