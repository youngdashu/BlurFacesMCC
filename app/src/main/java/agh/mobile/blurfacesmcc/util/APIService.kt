package agh.mobile.blurfacesmcc.util

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface APIService {
    @Multipart
    @POST("upload")
    suspend fun postUploadVideo(
        @Part file: MultipartBody.Part
    ): Response<String>
}