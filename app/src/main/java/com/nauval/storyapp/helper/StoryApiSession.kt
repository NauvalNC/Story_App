package com.nauval.storyapp.helper

import android.content.Context
import android.content.SharedPreferences

internal class StoryApiSession(ctx: Context) {
    private var pref: SharedPreferences = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setToken(token: String) = pref.edit().putString(TOKEN, token).apply()

    fun getToken(): String = pref.getString(TOKEN, "") ?: ""

    fun clearToken() = pref.edit().clear().apply()

    companion object {
        private const val PREF_NAME = "STORY_PREF"
        private const val TOKEN = "USER_TOKEN"
    }
}