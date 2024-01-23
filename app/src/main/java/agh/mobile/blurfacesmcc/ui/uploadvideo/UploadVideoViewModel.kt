package agh.mobile.blurfacesmcc.ui.uploadvideo

import agh.mobile.blurfacesmcc.ConfidentialData
import agh.mobile.blurfacesmcc.VideoRecord
import agh.mobile.blurfacesmcc.domain.RequestStatus
import agh.mobile.blurfacesmcc.repositories.DefaultVideosRepository
import agh.mobile.blurfacesmcc.ui.util.confidentialDataArrayStore
import agh.mobile.blurfacesmcc.ui.util.process.saveVideoToDataStore
import agh.mobile.blurfacesmcc.ui.util.videoDataStore
import agh.mobile.blurfacesmcc.workers.LocalBlurWorker
import agh.mobile.blurfacesmcc.workers.RemoteBlurWorker
import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableList
import java.io.IOException
import javax.inject.Inject


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
            .objectsList
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
}

@OptIn(ExperimentalCoroutinesApi::class)
fun AndroidViewModel.getWorkInfo(workTag: String): Flow<List<WorkInfo>> {
    return WorkManager.getInstance(getApplication())
        .getWorkInfosByTagFlow(workTag)
        .mapLatest {
            it.toImmutableList()
        }
}
