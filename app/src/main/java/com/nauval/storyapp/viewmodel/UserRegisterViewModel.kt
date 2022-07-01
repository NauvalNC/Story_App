package com.nauval.storyapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nauval.storyapp.helper.RegisterResponse
import com.nauval.storyapp.repository.UserRepository
import kotlinx.coroutines.launch

class UserRegisterViewModel(private val repository: UserRepository) : ViewModel() {
    private var _response = MutableLiveData<RegisterResponse>()
    val response: LiveData<RegisterResponse> = _response

    private var _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    fun registerUser(name: String, email: String, password: String) = viewModelScope.launch {
        try {
            val response = repository.registerUser(name, email, password)
            if (response.isSuccessful) {
                _response.postValue(response.body())
            }
            _isError.postValue(!response.isSuccessful)
        } catch (e: Exception) {
            _isError.postValue(true)
            e.printStackTrace()
        }
    }
}