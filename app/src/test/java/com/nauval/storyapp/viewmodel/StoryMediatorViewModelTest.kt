package com.nauval.storyapp.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.recyclerview.widget.ListUpdateCallback
import com.nauval.storyapp.DummyData
import com.nauval.storyapp.MainCoroutineRule
import com.nauval.storyapp.PagedStoryDataTestSources
import com.nauval.storyapp.adapter.ListStoryPagingAdapter
import com.nauval.storyapp.getOrAwaitValue
import com.nauval.storyapp.helper.StoryItemResponse
import com.nauval.storyapp.repository.StoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StoryMediatorViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRules = MainCoroutineRule()

    private lateinit var viewModel: StoryMediatorViewModel
    @Mock
    private lateinit var repository: StoryRepository

    @Before
    fun setUp() {
        viewModel = StoryMediatorViewModel(repository)
    }

    @Test
    fun `when get storyList success, then data should not null and correct as expected`() =
        mainCoroutineRules.runBlockingTest {
            val dummyStories = DummyData.generateDummyStories()
            val pagedData = MutableLiveData(PagedStoryDataTestSources.getSnapshot(dummyStories))
            `when`(repository.getStories(DummyData.dummyToken)).thenReturn(pagedData)

            val actualStory = viewModel.storyList(DummyData.dummyToken).getOrAwaitValue()
            verify(repository).getStories(DummyData.dummyToken)

            val diff = AsyncPagingDataDiffer(
                diffCallback = ListStoryPagingAdapter.DIFF_CALLBACK,
                updateCallback = storyListUpdateCallback,
                mainDispatcher = mainCoroutineRules.dispatcher,
                workerDispatcher = mainCoroutineRules.dispatcher,
            )
            diff.submitData(actualStory)

            advanceUntilIdle()

            val actualDataSnapshot = diff.snapshot()

            assertNotNull(actualDataSnapshot) // list story item should not null

            // list story item should same as expected
            assertEquals(dummyStories, actualDataSnapshot)
            assertEquals(dummyStories.size, actualDataSnapshot.size)
            assertEquals(dummyStories[0].id, actualDataSnapshot[0]?.id)
            assertEquals(dummyStories[0].name, actualDataSnapshot[0]?.name)
            assertEquals(dummyStories[0].description, actualDataSnapshot[0]?.description)
            assertEquals(dummyStories[0].createdAt, actualDataSnapshot[0]?.createdAt)
            assertEquals(dummyStories[0].photoUrl, actualDataSnapshot[0]?.photoUrl)
            assertEquals(dummyStories[0].lon, actualDataSnapshot[0]?.lon)
            assertEquals(dummyStories[0].lat, actualDataSnapshot[0]?.lat)
        }

    @Test
    fun `when get storyList failed, then data should be non-null empty since the room database will be empty`() =
        mainCoroutineRules.runBlockingTest {
            val dummyStories = emptyList<StoryItemResponse>()
            val pagedData = MutableLiveData(PagedStoryDataTestSources.getSnapshot(dummyStories))
            `when`(repository.getStories(DummyData.dummyToken)).thenReturn(pagedData)

            val actualStory = viewModel.storyList(DummyData.dummyToken).getOrAwaitValue()
            verify(repository).getStories(DummyData.dummyToken)

            val diff = AsyncPagingDataDiffer(
                diffCallback = ListStoryPagingAdapter.DIFF_CALLBACK,
                updateCallback = storyListUpdateCallback,
                mainDispatcher = mainCoroutineRules.dispatcher,
                workerDispatcher = mainCoroutineRules.dispatcher,
            )
            diff.submitData(actualStory)

            val actualStorySnapshot = diff.snapshot()
            advanceUntilIdle()

            assertNotNull(actualStorySnapshot) // list story item should not null
            assertEquals(
                dummyStories,
                actualStorySnapshot
            ) // list story item should empty as expected
            assertEquals(
                dummyStories.size,
                actualStorySnapshot.size
            ) // list story item should empty as expected
        }
}

val storyListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}