package com.nauval.storyapp.repository

import com.nauval.storyapp.api.UserApiService

class UserRepository (private val apiService: UserApiService) {

    suspend fun loginUser(email: String, password: String) =
        apiService.loginUser(email, password)

    suspend fun registerUser(name: String, email: String, password: String) =
        apiService.registerUser(name, email, password)
}