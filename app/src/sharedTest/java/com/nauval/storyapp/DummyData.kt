package com.nauval.storyapp

import com.nauval.storyapp.helper.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

object DummyData {
    private const val dummyUserId = "user123"
    const val dummyUsername = "Dummy Name"
    const val dummyEmail = "dummy@somemail.com"
    const val dummyPass = "1234567890"
    const val dummyToken = "123456Ht2jUp98H"

    val dummyDesc = "Dummy description here".toRequestBody("text/plain".toMediaType())
    val dummyLat = "123".toRequestBody("text/plain".toMediaType())
    val dummyLon = "321".toRequestBody("text/plain".toMediaType())

    // region Login Register Dummies
    fun getDummyLoginResponse_Success(): Response<LoginResponse> {
        return Response.success(
            LoginResponse(
                LoginResultResponse(
                    dummyUsername,
                    dummyUserId,
                    dummyToken
                ),
                false,
                "success"
            )
        )
    }

    fun getDummyLoginResponse_Error(): Response<LoginResponse> {
        return Response.error(500, "error".toResponseBody())
    }

    fun getDummyRegisterResponse_Success(): Response<RegisterResponse> {
        return Response.success(
            RegisterResponse(
                false,
                "user created"
            )
        )
    }

    fun getDummyRegisterResponse_Error(): Response<RegisterResponse> {
        return Response.error(500, "error".toResponseBody())
    }
    // endregion

    // region Story Business Dummies
    fun getDummyUploadStoryResponse_Success(): Response<StoryUploadResponse> {
        return Response.success(
            StoryUploadResponse(
                false,
                "success"
            )
        )
    }

    fun getDummyUploadStoryResponse_Error(): Response<StoryUploadResponse> {
        return Response.error(500, "error".toResponseBody())
    }

    fun getDummyStoryResponse_Success(): Response<StoryResponse> {
        val temp = generateDummyStories()
        return Response.success(StoryResponse(temp, false, "success"))
    }

    fun getDummyStoryResponse_Error(): Response<StoryResponse> {
        return Response.error(500, "error".toResponseBody())
    }

    fun generateStory(): StoryResponse {
        val temp = generateDummyStories()
        return StoryResponse(temp, false, "success")
    }
    // endregion

    fun generateDummyStories(): List<StoryItemResponse> {
        val list: MutableList<StoryItemResponse> = arrayListOf()

        for (i in 0..100) {
            val story = StoryItemResponse(
                id = i.toString(),
                name = "Username_${i.toString()}",
                description = "Description_${i.toString()}",
                photoUrl = "Photo_${i.toString()}",
                createdAt = "2022-04-16T15:15:09.145Z",
                lon = 123.0,
                lat = 231.0
            )
            list.add(story)
        }

        return list
    }
}