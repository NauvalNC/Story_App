package com.nauval.storyapp.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nauval.storyapp.DummyData
import com.nauval.storyapp.MainCoroutineRule
import com.nauval.storyapp.api.FakeStoryBusinessApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.MultipartBody
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StoryBusinessRepositoryTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRules = MainCoroutineRule()

    private lateinit var repository: StoryBusinessRepository
    private lateinit var apiService: FakeStoryBusinessApiService

    @Before
    fun setUp() {
        apiService = FakeStoryBusinessApiService()
        repository = StoryBusinessRepository(apiService)
    }

    // region uploadStory
    @Test
    fun `when uploadStory success, callback should be success, response body should not null and correct as expected`() =
        mainCoroutineRules.runBlockingTest {
            val expectedResponse = DummyData.getDummyUploadStoryResponse_Success()
            apiService.dummyUploadResponse = expectedResponse

            val dummyFile = mock(MultipartBody.Part::class.java)

            val actualResponse = repository.uploadStory(
                DummyData.dummyToken,
                dummyFile,
                DummyData.dummyDesc,
                DummyData.dummyLat,
                DummyData.dummyLon
            )

            assertTrue(actualResponse.isSuccessful) // should success callback
            assertNotNull(actualResponse.body()) // body response should not null

            // body response should correct as expected
            assertEquals(
                expectedResponse.body()!!.message,
                actualResponse.body()!!.message
            )
        }

    @Test
    fun `when uploadStory failed, callback should be fail (false) and response body should null`() =
        mainCoroutineRules.runBlockingTest {
            apiService.dummyUploadResponse = DummyData.getDummyUploadStoryResponse_Error()

            val dummyFile = mock(MultipartBody.Part::class.java)

            val actualResponse = repository.uploadStory(
                DummyData.dummyToken,
                dummyFile,
                DummyData.dummyDesc,
                DummyData.dummyLat,
                DummyData.dummyLon
            )

            assertFalse(actualResponse.isSuccessful) // should failed (false) callback
            assertNull(actualResponse.body()) // body response should null
        }
    // endregion

    // region getStoriesWithLocation
    @Test
    fun `when getStoriesWithLocation success, callback should be success, response body should not null and correct as expected`() =
        mainCoroutineRules.runBlockingTest {
            val expectedResponse = DummyData.getDummyStoryResponse_Success()
            apiService.dummyStoryResponse = expectedResponse

            val actualResponse = repository.getStoriesWithLocation(DummyData.dummyToken)

            assertTrue(actualResponse.isSuccessful) // should success callback
            assertNotNull(actualResponse.body()) // body response should not null

            // body response should correct as expected
            assertEquals(
                expectedResponse.body()!!.stories.size,
                actualResponse.body()!!.stories.size
            )
            assertEquals(expectedResponse.body()!!.message, actualResponse.body()!!.message)
            assertEquals(expectedResponse.body()!!.error, actualResponse.body()!!.error)
        }

    @Test
    fun `when getStoriesWithLocation failed, callback should be fail (false) and response body should null`() =
        mainCoroutineRules.runBlockingTest {
            apiService.dummyStoryResponse = DummyData.getDummyStoryResponse_Error()

            val actualResponse = repository.getStoriesWithLocation(DummyData.dummyToken)

            assertFalse(actualResponse.isSuccessful) // should failed (false) callback
            assertNull(actualResponse.body()) // body response should null
        }
    // endregion
}