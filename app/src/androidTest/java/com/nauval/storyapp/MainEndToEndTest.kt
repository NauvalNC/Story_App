package com.nauval.storyapp

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.nauval.storyapp.helper.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainEndToEndTest {
    @get:Rule
    val activity = ActivityScenarioRule(LoginActivity::class.java)

    private val dummyEmail = "childe@gmail.com"
    private val dummyPassword = "childe123"

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countIdlingRes)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countIdlingRes)
    }

    // Scenario: login, then go to setting, click logout, confirm logout, back to login layout
    @Test
    fun loginThenLogout() {
        Intents.init()

        onView(withId(R.id.email_field)).perform(replaceText(dummyEmail)).check(matches(withText(dummyEmail)))
        onView(withId(R.id.password_field)).perform(replaceText(dummyPassword)).check(matches(withText(dummyPassword)))
        onView(withId(R.id.login_btn)).perform(click())

        intended(hasComponent(MainLandingPageActivity::class.java.name))
        onView(withId(R.id.setting_menu)).perform(click())

        intended(hasComponent(SettingActivity::class.java.name))
        onView(withText(R.string.logout)).perform(click())
        onView(withText(R.string.are_you_sure)).check(matches(isDisplayed()))
        onView(withText(R.string.yes)).perform(click())

        intended(hasComponent(LoginActivity::class.java.name))
        onView(withId(R.id.login_btn)).check(matches(isDisplayed()))

        Intents.release()
    }

    // Scenario: login, then check if the map is displayed
    @Test
    fun loadStoryListMap() {
        Intents.init()

        onView(withId(R.id.email_field)).perform(replaceText(dummyEmail)).check(matches(withText(dummyEmail)))
        onView(withId(R.id.password_field)).perform(replaceText(dummyPassword)).check(matches(withText(dummyPassword)))
        onView(withId(R.id.login_btn)).perform(click())

        intended(hasComponent(MainLandingPageActivity::class.java.name))
        onView(withId(R.id.map)).check(matches(isDisplayed()))

        Intents.release()
    }

    // Scenario: login, click for story list bottom navigation, then check if the story list is displayed
    @Test
    fun loadStoryList() {
        Intents.init()

        onView(withId(R.id.email_field)).perform(replaceText(dummyEmail)).check(matches(withText(dummyEmail)))
        onView(withId(R.id.password_field)).perform(replaceText(dummyPassword)).check(matches(withText(dummyPassword)))
        onView(withId(R.id.login_btn)).perform(click())

        intended(hasComponent(MainLandingPageActivity::class.java.name))
        onView(withId(R.id.nav_story)).perform(click())
        onView(withId(R.id.story_rv)).check(matches(isDisplayed()))

        Intents.release()
    }

    /* Scenario: login, click for story list bottom navigation, check if the story list is displayed,
       click on the first item, check if the story details layout is displayed */
    @Test
    fun loadStoryDetail() {
        Intents.init()

        onView(withId(R.id.email_field)).perform(replaceText(dummyEmail)).check(matches(withText(dummyEmail)))
        onView(withId(R.id.password_field)).perform(replaceText(dummyPassword)).check(matches(withText(dummyPassword)))
        onView(withId(R.id.login_btn)).perform(click())

        intended(hasComponent(MainLandingPageActivity::class.java.name))
        onView(withId(R.id.nav_story)).perform(click())
        onView(withId(R.id.story_rv)).check(matches(isDisplayed()))

        onView(withId(R.id.story_rv)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        intended(hasComponent(StoryDetailsWithMapActivity::class.java.name))
        onView(withId(R.id.story_detail)).check(matches(isDisplayed()))

        Intents.release()
    }
}