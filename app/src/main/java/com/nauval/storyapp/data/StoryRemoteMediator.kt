package com.nauval.storyapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.nauval.storyapp.api.StoryPagingApiService
import com.nauval.storyapp.database.RemoteKeys
import com.nauval.storyapp.database.StoryDatabase
import com.nauval.storyapp.helper.StoryItemResponse
import com.nauval.storyapp.helper.wrapWithIdlingResource

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val token: String,
    private val database: StoryDatabase,
    private val apiService: StoryPagingApiService
) : RemoteMediator<Int, StoryItemResponse>() {

    override suspend fun initialize(): InitializeAction = initialAction

    override suspend fun load(loadType: LoadType, state: PagingState<Int, StoryItemResponse>): MediatorResult {
        try {
            val pageKey = when (loadType) {
                LoadType.REFRESH -> {
                    1
                }
                LoadType.PREPEND -> {
                    val remoteKey = getFirstItemRemoteKeys(state)
                    remoteKey?.prevKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKey != null)
                }
                LoadType.APPEND -> {
                    val remoteKey = getLastItemRemoteKeys(state)
                    remoteKey?.nextKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKey != null)
                }
            }

            // Behaviour for testing only
            if (invokeNoDataForTest) {
                database.withTransaction {
                    database.storyDao().deleteAllStories()
                    database.remoteKeysDao().deleteAllRemoteKeys()
                }
            }

            val incomingStoryList = wrapWithIdlingResource {
                apiService.getStoryPages(
                    "Bearer $token",
                    pageKey,
                    state.config.pageSize
                ).stories
            }

            val isEndOfPage = incomingStoryList.isEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.storyDao().deleteAllStories()
                    database.remoteKeysDao().deleteAllRemoteKeys()
                }

                val prevKey = if (pageKey == 1) null else pageKey - 1
                val nextKey = if (isEndOfPage) null else pageKey + 1
                val newStoryRemoteKeys = incomingStoryList.map {
                    RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                }

                database.remoteKeysDao().insertRemoteKeys(newStoryRemoteKeys)
                database.storyDao().insertStories(incomingStoryList)
            }

            return MediatorResult.Success(endOfPaginationReached = isEndOfPage)
        } catch (exception: Exception) {
            return MediatorResult.Error(exception)
        }
    }

    //region Get Remote Keys
    private suspend fun getClosestCurrentRemoteKey(state: PagingState<Int, StoryItemResponse>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.remoteKeysDao().getRemoteKeysId(id)
            }
        }
    }

    private suspend fun getFirstItemRemoteKeys(state: PagingState<Int, StoryItemResponse>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }

    private suspend fun getLastItemRemoteKeys(state: PagingState<Int, StoryItemResponse>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }
    //endregion

    companion object {
        var initialAction = InitializeAction.SKIP_INITIAL_REFRESH
        var invokeNoDataForTest = false
    }
}