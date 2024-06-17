package com.example.wondrobe.utils

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Headers

interface BackgroundRemovalService {
    @Multipart
    @POST("remove-bg")
    @Headers("Authorization: Bearer gsULYXR3Z6Cbt7Z6QvjV5XAf")
    fun removeBackground(@Part image: MultipartBody.Part): Call<ResponseBody>
}
