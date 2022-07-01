package com.nauval.storyapp

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.paging.ExperimentalPagingApi
import androidx.paging.RemoteMediator
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.nauval.storyapp.data.StoryRemoteMediator
import com.nauval.storyapp.helper.EspressoIdlingResource
import com.nauval.storyapp.helper.StoryApiConfig
import com.nauval.storyapp.helper.Utils
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class StoryListFragmentTest {
    private val mockWebServer = MockWebServer()
    private val dummyUsername = "gege"
    private val dummySuccessResponse = "getallstory_success.json"

    @OptIn(ExperimentalPagingApi::class)
    @Before
    fun setUp() {
        mockWebServer.start(8080)
        StoryApiConfig.BASE_URL = mockWebServer.url("/").toString()
        StoryRemoteMediator.initialAction = RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countIdlingRes)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countIdlingRes)
    }

    // Scenario: open fragment, list is displayed, list should have the correct item
    @Test
    fun getStoryList_Success() {
        StoryRemoteMediator.invokeNoDataForTest = false

        launchFragmentInContainer<StoryListFragment>(themeResId = R.style.Theme_StoryApp)

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(Utils.readFileToString(dummySuccessResponse))
        mockWebServer.enqueue(mockResponse)

        onView(withId(R.id.story_rv)).check(matches(isDisplayed()))

        onView(withId(R.id.story_rv)).perform(
            RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText(dummyUsername))
            )
        )

        onView(withText(dummyUsername)).check(matches(isDisplayed()))
    }

    // Scenario: open fragment, list is empty, then layout for no data should be displayed
    @Test
    fun getStoryList_Failed() {
        // Needed to clear story at local room database
        StoryRemoteMediator.invokeNoDataForTest = true

        launchFragmentInContainer<StoryListFragment>(themeResId = R.style.Theme_StoryApp)

        val mockResponse = MockResponse()
            .setResponseCode(500)
        mockWebServer.enqueue(mockResponse)

        onView(withId(R.id.no_data)).check(matches(isDisplayed()))
    }
}