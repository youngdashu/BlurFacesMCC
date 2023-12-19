package agh.mobile.blurfacesmcc.ui.uploadvideo

import agh.mobile.blurfacesmcc.VideoRecord
import agh.mobile.blurfacesmcc.ui.util.videoDataStore
import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadVideoViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    fun uploadVideoForProcessing(context: Context, uri: Uri) {

    }

    fun saveUploadedPhotoURI(uri: Uri) {
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