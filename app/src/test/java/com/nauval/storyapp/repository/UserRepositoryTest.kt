package com.nauval.storyapp.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nauval.storyapp.DummyData
import com.nauval.storyapp.MainCoroutineRule
import com.nauval.storyapp.api.FakeUserApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class UserRepositoryTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRules = MainCoroutineRule()

    private lateinit var repository: UserRepository
    private lateinit var apiService: FakeUserApiService

    @Before
    fun setUp() {
        apiService = FakeUserApiService()
        repository = UserRepository(apiService)
    }

    // region loginUser
    @Test
    fun `when loginUser success, callback should be success, response body should not null and correct as expected`() =
        mainCoroutineRules.runBlockingTest {
            val expectedResponse = DummyData.getDummyLoginResponse_Success()
            apiService.dummyLoginResponse = expectedResponse

            val actualResponse = repository.loginUser(DummyData.dummyEmail, DummyData.dummyPass)

            assertTrue(actualResponse.isSuccessful) // should success callback
            assertNotNull(actualResponse.body()) // body response should not null

            // body response should correct as expected
            assertEquals(
                expectedResponse.body()!!.loginResultResponse.userId,
                actualResponse.body()!!.loginResultResponse.userId
            )
            assertEquals(
                expectedResponse.body()!!.loginResultResponse.name,
                actualResponse.body()!!.loginResultResponse.name
            )
            assertEquals(
                expectedResponse.body()!!.loginResultResponse.token,
                actualResponse.body()!!.loginResultResponse.token
            )
            assertEquals(
                expectedResponse.body()!!.message,
                actualResponse.body()!!.message
            )
            assertEquals(
                expectedResponse.body()!!.error,
                actualResponse.body()!!.error
            )
        }

    @Test
    fun `when loginUser failed, callback should be fail (false) and response body should null`() =
        mainCoroutineRules.runBlockingTest {
            apiService.dummyLoginResponse = DummyData.getDummyLoginResponse_Error()

            val actualResponse = repository.loginUser(DummyData.dummyEmail, DummyData.dummyPass)

            assertFalse(actualResponse.isSuccessful) // should failed (false) callback
            assertNull(actualResponse.body()) // body response should null
        }
    // endregion

    // region registerUser
    @Test
    fun `when registerUser success, callback should be success, response body should not null and correct as expected`() =
        mainCoroutineRules.runBlockingTest {
            val expectedResponse = DummyData.getDummyRegisterResponse_Success()
            apiService.dummyRegisterResponse = expectedResponse

            val actualResponse = repository.registerUser(
                DummyData.dummyUsername,
                DummyData.dummyEmail,
                DummyData.dummyPass
            )

            assertTrue(actualResponse.isSuccessful) // should success callback
            assertNotNull(actualResponse.body()) // body response should not null

            // body response should correct as expected
            assertEquals(
                expectedResponse.body()!!.message,
                actualResponse.body()!!.message
            )
            assertEquals(
                expectedResponse.body()!!.error,
                actualResponse.body()!!.error
            )
        }

    @Test
    fun `when registerUser failed, callback should be fail (false) and response body should null`() =
        mainCoroutineRules.runBlockingTest {
            apiService.dummyRegisterResponse = DummyData.getDummyRegisterResponse_Error()

            val actualResponse = repository.registerUser(
                DummyData.dummyUsername,
                DummyData.dummyEmail,
                DummyData.dummyPass
            )

            assertFalse(actualResponse.isSuccessful) // should failed (false) callback
            assertNull(actualResponse.body()) // body response should null
        }
    // endregion
}