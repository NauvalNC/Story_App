package com.nauval.storyapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nauval.storyapp.helper.LoginResponse
import com.nauval.storyapp.repository.UserRepository
import kotlinx.coroutines.launch

class UserLoginViewModel (private val repository: UserRepository): ViewModel() {
    private var _response = MutableLiveData<LoginResponse>()
    val response: LiveData<LoginResponse> = _response

    private var _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    fun loginUser(email: String, password: String) = viewModelScope.launch {
        try {
            val response = repository.loginUser(email, password)
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