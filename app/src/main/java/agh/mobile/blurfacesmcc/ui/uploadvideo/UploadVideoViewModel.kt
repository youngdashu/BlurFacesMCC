package agh.mobile.blurfacesmcc.ui.uploadvideo

import agh.mobile.blurfacesmcc.VideoRecord
import agh.mobile.blurfacesmcc.domain.RequestStatus
import agh.mobile.blurfacesmcc.domain.requestTypes.UploadVideoRequest
import agh.mobile.blurfacesmcc.repositories.DefaultVideosRepository
import agh.mobile.blurfacesmcc.ui.util.process.saveVideoToDataStore
import agh.mobile.blurfacesmcc.ui.util.videoDataStore
import agh.mobile.blurfacesmcc.util.APIService
import agh.mobile.blurfacesmcc.workers.LocalBlurWorker
import agh.mobile.blurfacesmcc.workers.RemoteBlurWorker
import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.toImmutableList
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject


const val REQUESTS_NUM: Int = 16

data class FacesAtFrame(
    val faces: List<Face>,
    val frameIndex: Int,
    val frame: Bitmap
)

@HiltViewModel
class UploadVideoViewModel @Inject constructor(
    private val application: Application,
    private val videosRepository: DefaultVideosRepository
) : AndroidViewModel(application) {

    val videoTitle = MutableStateFlow("")
    val uploadStatus = MutableStateFlow(RequestStatus.NOT_SEND)
    val errorMessage: MutableStateFlow<String?> = MutableStateFlow(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val processingProgress = getWorkInfo("localBlur").mapLatest {
        it.firstOrNull()?.progress?.getFloat(LocalBlurWorker.Progress, 0f) ?: 0f
    }

    fun extractFacesFromVideo(videoUri: Uri, onFinish: (String?) -> Unit) {
        updateUploadStatus(RequestStatus.WAITING)

        val blurWorkerRequest = OneTimeWorkRequestBuilder<LocalBlurWorker>()
            .setInputData(
                workDataOf(
                    LocalBlurWorker.URI_KEY to videoUri.toString(),
                    LocalBlurWorker.VIDEO_TITLE_KEY to videoTitle.value,
                    LocalBlurWorker.NOTIFICATION_ID_KEY to 1
                )
            )
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("localBlur")
            .build()


        val operation = WorkManager.getInstance(application)
            .enqueueUniqueWork(
                "localBLur",
                ExistingWorkPolicy.REPLACE,
                blurWorkerRequest
            )

        viewModelScope.launch {
            runCatching {
                updateUploadStatus(RequestStatus.SUCCESS)
                onFinish("Video submitted successfully")
            }.exceptionOrNull()?.let {
                updateUploadStatus(RequestStatus.NOT_SEND) // FAILURE HERE
                onFinish("Error: $it")
            }
        }
    }

    private fun updateUploadStatus(newStatus: RequestStatus) {
        uploadStatus.update { newStatus }
    }

    fun updateVideoTitle(newTitle: String) = videoTitle.update { newTitle }


    fun updateErrorMessage(newMessage: String) = errorMessage.update { newMessage }

    fun uploadVideoForProcessing(
        videoUri: Uri,
        videoTitle: String,
        navigateToHomePage: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            updateUploadStatus(RequestStatus.WAITING)
            val blurWorkerRequest = OneTimeWorkRequestBuilder<RemoteBlurWorker>()
                .setInputData(
                    workDataOf(
                        RemoteBlurWorker.URI_KEY to videoUri.toString(),
                        RemoteBlurWorker.VIDEO_TITLE_KEY to videoTitle,
                    )
                )
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .addTag("remoteBlur")
                .build()
            application.saveVideoToDataStore {
                uri = videoUri.toString()
                filename = videoTitle
                blured = false
                this.jobId = jobId.toString()
            }
            val operation = WorkManager.getInstance(application)
                .enqueueUniqueWork(
                    "remoteBLur",
                    ExistingWorkPolicy.REPLACE,
                    blurWorkerRequest
                )
        }.invokeOnCompletion {
            viewModelScope.launch(Dispatchers.Main) {
                when (it?.cause) {
                    is IOException -> {
                        Toast.makeText(
                            getApplication(),
                            "An error Occurred ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        updateUploadStatus(RequestStatus.NOT_SEND)
                    }

                    else -> {
                        if (it == null) {
                            Toast.makeText(
                                getApplication(),
                                "Task scheduled successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            updateUploadStatus(RequestStatus.SUCCESS)
                            navigateToHomePage()
                        }
                    }
                }
            }

        }
    }

    private suspend fun videoExists(fileName: String): Boolean =
        application
            .videoDataStore
            .data
            .first()
            .toBuilder()
            .objectsList.run {
                Log.d("xdd", this.joinToString { "," })
                this
            }
            .filter { element -> element.filename == fileName }
            .size
            .run { this > 0 }


    fun processIfVideoDoesNotExist(fileName: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        viewModelScope.launch {
            if (videoExists(fileName)) {
                onFailure()
            } else {
                onSuccess()
            }
        }
    }

    fun saveUploadedVideoURI(uri: Uri) {
        viewModelScope.launch {
            getApplication<Application>().videoDataStore.updateData { videos ->
                getApplication<Application>()
                    .contentResolver
                    .query(uri, null, null, null, null)
                    .use { cursor ->
                        val nameIndex = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        cursor.moveToFirst()
                        videos.toBuilder().addObjects(
                            VideoRecord.getDefaultInstance().toBuilder()
                                .setUri(uri.toString())
                                .setFilename(cursor.getString(nameIndex))
                                .setBlured(true)
                        ).build()
                    }
            }
        }
    }


    fun stressTest(videoUri: Uri, videoTitleInput: String) {

        val inputStream = application
            .contentResolver
            .openInputStream(Uri.parse(videoUri.toString()))!!
        val file = inputStream.readAllBytes()
        inputStream.close()

        viewModelScope.launch {

            val videoTitle = videoTitleInput
            val uploadVideoRequest = UploadVideoRequest(file, videoTitle)

            val video = uploadVideoRequest.file.toRequestBody(
                "video/mp4".toMediaTypeOrNull(),
                0,
                uploadVideoRequest.file.size
            )
            val fileVideo =
                MultipartBody.Part.createFormData(
                    "file",
                    uploadVideoRequest.fileName + ".mp4",
                    video
                )

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
                .create(APIService::class.java)

            (0..<REQUESTS_NUM).forEach {
                launch {
                    val res = retrofit
                        .postUploadVideo(fileVideo)
                    println("$it:\n${res.body()}\n")
                }

            }

        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun AndroidViewModel.getWorkInfo(workTag: String): Flow<List<WorkInfo>> {
    return WorkManager.getInstance(getApplication())
        .getWorkInfosByTagFlow(workTag)
        .mapLatest {
            it.toImmutableList()
        }
}
