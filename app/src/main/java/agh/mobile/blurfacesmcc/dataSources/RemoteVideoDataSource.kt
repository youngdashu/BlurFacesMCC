package agh.mobile.blurfacesmcc.dataSources

import agh.mobile.blurfacesmcc.domain.requestTypes.UploadVideoRequest
import agh.mobile.blurfacesmcc.util.APIService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

class RemoteVideoDataSource @Inject constructor(
    private val retrofit: Retrofit,
) {
    suspend fun upload(uploadVideoRequest: UploadVideoRequest): Response<Unit> {
        val video = uploadVideoRequest.file.toRequestBody(
            "video/mp4".toMediaTypeOrNull(),
            0,
            uploadVideoRequest.file.size
        )
        val fileVideo =
            MultipartBody.Part.createFormData("file", uploadVideoRequest.fileName + ".mp4", video)
        return retrofit
            .create(APIService::class.java)
            .postUploadVideo(fileVideo)
    }
}