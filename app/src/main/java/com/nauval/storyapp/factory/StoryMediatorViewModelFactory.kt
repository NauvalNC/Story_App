package com.nauval.storyapp.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nauval.storyapp.database.StoryDatabase
import com.nauval.storyapp.helper.StoryApiConfig
import com.nauval.storyapp.repository.StoryRepository
import com.nauval.storyapp.viewmodel.StoryMediatorViewModel

class StoryMediatorViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryMediatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoryMediatorViewModel(
                StoryRepository(
                    StoryDatabase.getDatabase(context),
                    StoryApiConfig.getStoryPagingApiService()
                )
            ) as T
        }
        throw IllegalArgumentException("${modelClass.name} is not supported by this factory")
    }
}