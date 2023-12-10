package agh.mobile.blurfacesmcc.ui.uploadvideo

import agh.mobile.blurfacesmcc.VideoRecord
import agh.mobile.blurfacesmcc.ui.util.videoDataStore
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadVideoViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    fun saveUploadedPhotoURI(uri: Uri) {
        viewModelScope.launch {
            getApplication<Application>().videoDataStore.updateData {
                it.toBuilder().addObjects(
                    VideoRecord.getDefaultInstance().toBuilder().setUri(uri.toString())
                        .setFilename("xd")
                ).build()
            }
        }

    }
}