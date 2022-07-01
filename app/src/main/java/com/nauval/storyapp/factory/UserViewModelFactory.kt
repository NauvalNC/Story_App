package com.nauval.storyapp.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nauval.storyapp.repository.UserRepository
import com.nauval.storyapp.viewmodel.UserLoginViewModel
import com.nauval.storyapp.viewmodel.UserRegisterViewModel

class UserViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserLoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserLoginViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(UserRegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserRegisterViewModel(repository) as T
        }
        throw IllegalArgumentException("${modelClass.name} is not supported by this factory")
    }
}