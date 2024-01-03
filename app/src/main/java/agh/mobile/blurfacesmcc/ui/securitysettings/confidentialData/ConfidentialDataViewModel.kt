package agh.mobile.blurfacesmcc.ui.securitysettings.confidentialData

import agh.mobile.blurfacesmcc.ConfidentialData
import agh.mobile.blurfacesmcc.ui.util.confidentialDataArrayStore
import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfidentialDataViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    val confidentialData = MutableStateFlow<List<ConfidentialData>?>(null)

    init {
        viewModelScope.launch {
            application.confidentialDataArrayStore.data.onEach { array ->
                confidentialData.update {
                    array.objectsList
                }
            }.collect()
        }

    }

    fun saveUri(uri: Uri) {
        viewModelScope.launch {
            getApplication<Application>().confidentialDataArrayStore.updateData { array ->
                getApplication<Application>()
                    .contentResolver
                    .query(uri, null, null, null, null)
                    .use { cursor ->
                        val nameIndex = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        cursor.moveToFirst()
                        array.toBuilder().addObjects(
                            ConfidentialData.getDefaultInstance().toBuilder()
                                .setUri(uri.toString())
                                .setFilename(cursor.getString(nameIndex))
                        ).build()
                    }
            }
        }
    }
}