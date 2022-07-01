package com.nauval.storyapp.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nauval.storyapp.DummyData
import com.nauval.storyapp.MainCoroutineRule
import com.nauval.storyapp.getOrAwaitValue
import com.nauval.storyapp.helper.StoryResponse
import com.nauval.storyapp.helper.StoryUploadResponse
import com.nauval.storyapp.repository.StoryBusinessRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.MultipartBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response

@ExperimentalCoroutinesApi
@Suppress("UNCHECKED_CAST")
@RunWith(MockitoJUnitRunner::class)
class StoryBusinessViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRules = MainCoroutineRule()

    @Mock
    private lateinit var repository: StoryBusinessRepository
    private lateinit var viewModel: StoryBusinessViewModel

    @Before
    fun setUp() {
        viewModel = StoryBusinessViewModel(repository)
    }

    // region getStoriesWithLocation
    @Test
    fun `when getStoriesWithLocation success, the callback should should be success, the data should not null, data are correct as expected, and isError should be false`() =
        mainCoroutineRules.runBlockingTest {
            val response = mock(Response::class.java) as Response<StoryResponse>
            val responseBody = mock(StoryResponse::class.java)
            val expectedValue = DummyData.generateDummyStories()

            `when`(responseBody.stories).thenReturn(expectedValue)
            `when`(response.isSuccessful).thenReturn(true)
            `when`(response.body()).thenReturn(responseBody)
            `when`(repository.getStoriesWithLocation(DummyData.dummyToken)).thenReturn(response)

            viewModel.getStoriesWithLocation(DummyData.dummyToken)
            verify(repository).getStoriesWithLocation(DummyData.dummyToken)
            verify(response).body()
            verify(responseBody).stories

            val actualValue = viewModel.storiesWithLocation.getOrAwaitValue()

            assertTrue(response.isSuccessful) // should success callback
            assertNotNull(actualValue) // should not null
            assertEquals(expectedValue, actualValue) // should be the same data
            assertEquals(expectedValue.size, actualValue.size) // should be the same size
            assertFalse(viewModel.isError.getOrAwaitValue()) // should isError false
        }

    @Test
    fun `when getStoriesWithLocation failed, callback should be fail (false) and isError should be true`() =
        mainCoroutineRules.runBlockingTest {
            val response = mock(Response::class.java) as Response<StoryResponse>

            `when`(response.isSuccessful).thenReturn(false)
            `when`(repository.getStoriesWithLocation(DummyData.dummyToken)).thenReturn(response)

            viewModel.getStoriesWithLocation(DummyData.dummyToken)
            verify(repository).getStoriesWithLocation(DummyData.dummyToken)

            assertFalse(response.isSuccessful) // should failed (false) callback
            assertTrue(viewModel.isError.getOrAwaitValue()) // should isError false
        }

    @Throws(Exception::class)
    @Test
    fun `when getStoriesWithLocation caused exception, isError should be true`() =
        mainCoroutineRules.runBlockingTest {
            try {
                `when`(repository.getStoriesWithLocation(DummyData.dummyToken)).thenReturn(null)
                viewModel.getStoriesWithLocation(DummyData.dummyToken)
                verify(repository).getStoriesWithLocation(DummyData.dummyToken)
            } catch (exp: Exception) {
                assertTrue(viewModel.isError.getOrAwaitValue()) // should isError true
            }
        }
    // endregion

    // region uploadStory
    @Test
    fun `when uploadStory success, callback should be success and isError should be false`() =
        mainCoroutineRules.runBlockingTest {
            val response = mock(Response::class.java) as Response<StoryUploadResponse>
            val responseBody = mock(StoryUploadResponse::class.java)
            val dummyFile = mock(MultipartBody.Part::class.java)

            `when`(response.isSuccessful).thenReturn(true)
            `when`(response.body()).thenReturn(responseBody)
            `when`(
                repository.uploadStory(
                    DummyData.dummyToken,
                    dummyFile,
                    DummyData.dummyDesc,
                    DummyData.dummyLat,
                    DummyData.dummyLon
                )
            ).thenReturn(response)

            viewModel.uploadStory(
                DummyData.dummyToken,
                dummyFile,
                DummyData.dummyDesc,
                DummyData.dummyLat,
                DummyData.dummyLon
            )
            verify(repository).uploadStory(
                DummyData.dummyToken,
                dummyFile,
                DummyData.dummyDesc,
                DummyData.dummyLat,
                DummyData.dummyLon
            )
            verify(response).body()

            assertTrue(response.isSuccessful) // should success callback
            assertFalse(viewModel.isError.getOrAwaitValue()) // should isError false
        }

    @Test
    fun `when uploadStory failed, callback should be fail (false) and isError should be true`() = mainCoroutineRules.runBlockingTest {
        val response = mock(Response::class.java) as Response<StoryUploadResponse>
        val dummyFile = mock(MultipartBody.Part::class.java)

        `when`(response.isSuccessful).thenReturn(false)
        `when`(
            repository.uploadStory(
                DummyData.dummyToken,
                dummyFile,
                DummyData.dummyDesc,
                DummyData.dummyLat,
                DummyData.dummyLon
            )
        ).thenReturn(response)

        viewModel.uploadStory(
            DummyData.dummyToken,
            dummyFile,
            DummyData.dummyDesc,
            DummyData.dummyLat,
            DummyData.dummyLon
        )
        verify(repository).uploadStory(
            DummyData.dummyToken,
            dummyFile,
            DummyData.dummyDesc,
            DummyData.dummyLat,
            DummyData.dummyLon
        )

        assertFalse(response.isSuccessful) // should failed (false) callback
        assertTrue(viewModel.isError.getOrAwaitValue()) // should isError true
    }

    @Throws(Exception::class)
    @Test
    fun `when uploadStory caused exception, isError should be true`() = mainCoroutineRules.runBlockingTest {
        try {
            val dummyFile = mock(MultipartBody.Part::class.java)
            `when`(
                repository.uploadStory(
                    DummyData.dummyToken,
                    dummyFile,
                    DummyData.dummyDesc,
                    DummyData.dummyLat,
                    DummyData.dummyLon
                )
            ).thenReturn(null)

            viewModel.uploadStory(
                DummyData.dummyToken,
                dummyFile,
                DummyData.dummyDesc,
                DummyData.dummyLat,
                DummyData.dummyLon
            )
            verify(repository).uploadStory(
                DummyData.dummyToken,
                dummyFile,
                DummyData.dummyDesc,
                DummyData.dummyLat,
                DummyData.dummyLon
            )
        } catch (e: Exception) {
            assertTrue(viewModel.isError.getOrAwaitValue()) // should isError true
        }
    }
    // endregion
}