package com.nauval.storyapp.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nauval.storyapp.repository.StoryBusinessRepository
import com.nauval.storyapp.viewmodel.StoryBusinessViewModel

class StoryBusinessViewModelFactory(private val repository: StoryBusinessRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryBusinessViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoryBusinessViewModel(repository) as T
        }
        throw IllegalArgumentException("${modelClass.name} is not supported by this factory")
    }
}