package com.nauval.storyapp.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nauval.storyapp.DummyData
import com.nauval.storyapp.MainCoroutineRule
import com.nauval.storyapp.getOrAwaitValue
import com.nauval.storyapp.helper.RegisterResponse
import com.nauval.storyapp.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response

@ExperimentalCoroutinesApi
@Suppress("UNCHECKED_CAST")
@RunWith(MockitoJUnitRunner::class)
class UserRegisterViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRules = MainCoroutineRule()

    @Mock private lateinit var repository: UserRepository
    private lateinit var viewModel: UserRegisterViewModel

    @Before
    fun setUp() {
        viewModel = UserRegisterViewModel(repository)
    }

    @Test
    fun `when registerUser success, callback should be success and isError should be false`() =
        mainCoroutineRules.runBlockingTest {
            val response = mock(Response::class.java) as Response<RegisterResponse>
            `when`(response.isSuccessful).thenReturn(true)
            `when`(
                repository.registerUser(
                    DummyData.dummyUsername,
                    DummyData.dummyEmail,
                    DummyData.dummyPass
                )
            ).thenReturn(response)

            viewModel.registerUser(
                DummyData.dummyUsername,
                DummyData.dummyEmail,
                DummyData.dummyPass
            )
            Mockito.verify(repository)
                .registerUser(DummyData.dummyUsername, DummyData.dummyEmail, DummyData.dummyPass)

            assertTrue(response.isSuccessful) // should success callback
            assertFalse(viewModel.isError.getOrAwaitValue()) // should isError false
        }

    @Test
    fun `when registerUser failed, callback should be fail (false) and isError should be true`() =
        mainCoroutineRules.runBlockingTest {
            val response = mock(Response::class.java) as Response<RegisterResponse>
            `when`(response.isSuccessful).thenReturn(false)
            `when`(
                repository.registerUser(
                    DummyData.dummyUsername,
                    DummyData.dummyEmail,
                    DummyData.dummyPass
                )
            ).thenReturn(response)

            viewModel.registerUser(
                DummyData.dummyUsername,
                DummyData.dummyEmail,
                DummyData.dummyPass
            )
            Mockito.verify(repository)
                .registerUser(DummyData.dummyUsername, DummyData.dummyEmail, DummyData.dummyPass)

            assertFalse(response.isSuccessful) // should failed (false) callback
            assertTrue(viewModel.isError.getOrAwaitValue()) // should isError true
        }

    @Throws(Exception::class)
    @Test
    fun `when registerUser caused exception, isError should be true`() =
        mainCoroutineRules.runBlockingTest {
            try {
                `when`(
                    repository.registerUser(
                        DummyData.dummyUsername,
                        DummyData.dummyEmail,
                        DummyData.dummyPass
                    )
                ).thenReturn(null)
                viewModel.registerUser(
                    DummyData.dummyUsername,
                    DummyData.dummyEmail,
                    DummyData.dummyPass
                )

                Mockito.verify(repository).registerUser(
                    DummyData.dummyUsername,
                    DummyData.dummyEmail,
                    DummyData.dummyPass
                )
            } catch (e: Exception) {
                assertTrue(viewModel.isError.getOrAwaitValue()) // should isError true
            }
        }
}