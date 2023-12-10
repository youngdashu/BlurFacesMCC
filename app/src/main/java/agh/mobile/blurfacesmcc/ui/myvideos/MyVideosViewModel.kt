package agh.mobile.blurfacesmcc.ui.myvideos

import agh.mobile.blurfacesmcc.VideoRecord
import agh.mobile.blurfacesmcc.ui.util.videoDataStore
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyVideosViewModel(
    application: Application
) : AndroidViewModel(application) {

    val videoRecords = MutableStateFlow<List<VideoRecord>?>(null)

    init {
        viewModelScope.launch {
            application.videoDataStore.data.onEach { videos ->
                videoRecords.update {
                    videos.objectsList
                }
            }.collect()
        }

    }

}