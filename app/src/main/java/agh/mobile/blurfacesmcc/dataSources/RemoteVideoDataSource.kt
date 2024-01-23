package agh.mobile.blurfacesmcc.dataSources

import agh.mobile.blurfacesmcc.domain.requestTypes.UploadVideoRequest
import agh.mobile.blurfacesmcc.util.APIService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RemoteVideoDataSource @Inject constructor(
//    private val retrofit: Retrofit,
) {
    suspend fun upload(uploadVideoRequest: UploadVideoRequest): Response<String> {
        val video = uploadVideoRequest.file.toRequestBody(
            "video/mp4".toMediaTypeOrNull(),
            0,
            uploadVideoRequest.file.size
        )
        val fileVideo =
            MultipartBody.Part.createFormData("file", uploadVideoRequest.fileName + ".mp4", video)
        val retrofit = Retrofit
            .Builder()
            //.baseUrl("http://blur-server.default.54.221.201.107.sslip.io")
            .baseUrl("http://blur-server.default.54.167.133.117.sslip.io")
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(200, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .writeTimeout(200, TimeUnit.SECONDS)
                    .callTimeout(Duration.ofSeconds(200))
                    .readTimeout(200, TimeUnit.SECONDS).build()
            )
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        return retrofit
            .create(APIService::class.java)
            .postUploadVideo(fileVideo)
    }
}