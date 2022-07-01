package com.nauval.storyapp.api

import com.nauval.storyapp.DummyData
import com.nauval.storyapp.helper.LoginResponse
import com.nauval.storyapp.helper.RegisterResponse
import retrofit2.Response

class FakeUserApiService: UserApiService {
    var dummyLoginResponse: Response<LoginResponse> =
        DummyData.getDummyLoginResponse_Success()

    var dummyRegisterResponse: Response<RegisterResponse> =
        DummyData.getDummyRegisterResponse_Success()

    override suspend fun loginUser(email: String, password: String): Response<LoginResponse> {
        return dummyLoginResponse
    }

    override suspend fun registerUser(
        name: String,
        email: String,
        password: String
    ): Response<RegisterResponse> {
        return dummyRegisterResponse
    }
}