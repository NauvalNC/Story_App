package com.nauval.storyapp.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nauval.storyapp.DummyData
import com.nauval.storyapp.MainCoroutineRule
import com.nauval.storyapp.api.FakeStoryPagingApiService
import com.nauval.storyapp.api.StoryPagingApiService
import com.nauval.storyapp.database.FakeStoryDao
import com.nauval.storyapp.database.StoryDatabase
import com.nauval.storyapp.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StoryRepositoryTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRules = MainCoroutineRule()

    private lateinit var repository: StoryRepository
    @Mock private lateinit var database: StoryDatabase
    private lateinit var apiService: StoryPagingApiService
    private lateinit var storyDao: FakeStoryDao

    @Before
    fun setUp() {
        storyDao = FakeStoryDao()
        apiService = FakeStoryPagingApiService()
        repository = StoryRepository(database, apiService)
    }

    @Test
    fun `when getStories list success, then should return non-null data`() {
        mainCoroutineRules.runBlockingTest {
            `when`(database.storyDao()).thenReturn(storyDao)
            val actualValue = repository.getStories(DummyData.dummyToken).getOrAwaitValue()
            assertNotNull(actualValue)
        }
    }
}