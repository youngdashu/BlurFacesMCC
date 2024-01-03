package agh.mobile.blurfacesmcc.ui.uploadvideo

import agh.mobile.blurfacesmcc.VideoRecord
import agh.mobile.blurfacesmcc.domain.RequestStatus
import agh.mobile.blurfacesmcc.domain.requestTypes.UploadVideoRequest
import agh.mobile.blurfacesmcc.repositories.DefaultVideosRepository
import agh.mobile.blurfacesmcc.ui.util.videoDataStore
import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class UploadVideoViewModel @Inject constructor(
    private val application: Application,
    private val videosRepository: DefaultVideosRepository
) : AndroidViewModel(application) {

    val uploadStatus = MutableStateFlow(RequestStatus.NOT_SEND)

    fun updateUploadStatus(newStatus: RequestStatus) {
        uploadStatus.update { newStatus }
    }

    fun uploadVideoForProcessing(uri: Uri, videoTitle: String, navigateToHomePage: () -> Unit) {
        viewModelScope.launch {
            updateUploadStatus(RequestStatus.WAITING)
            val inputStream = getApplication<Application>()
                .contentResolver
                .openInputStream(uri)!!
            val file = inputStream.readAllBytes()
            inputStream.close()
            videosRepository.processRemote(UploadVideoRequest(file, videoTitle))
        }.invokeOnCompletion {
            when (it?.cause) {
                is IOException -> {
                    Toast.makeText(getApplication(), "An error Occurred", Toast.LENGTH_SHORT).show()
                    updateUploadStatus(RequestStatus.NOT_SEND)
                }

                else -> {
                    if (it == null) {
                        Toast.makeText(
                            getApplication(),
                            "Video Uploaded Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        updateUploadStatus(RequestStatus.SUCCESS)
                        navigateToHomePage()
                    }
                }
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