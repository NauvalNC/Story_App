package com.nauval.storyapp.helper

import androidx.test.espresso.idling.CountingIdlingResource

object EspressoIdlingResource {
    private const val RESOURCE = "GLOBAL"
    @JvmField val countIdlingRes = CountingIdlingResource(RESOURCE)

    fun increment() { countIdlingRes.increment() }
    fun decrement() { if (!countIdlingRes.isIdleNow) countIdlingRes.decrement() }
}

inline fun <T> wrapWithIdlingResource(function: () -> T): T {
    EspressoIdlingResource.increment()
    return try { function() } finally { EspressoIdlingResource.decrement() }
}