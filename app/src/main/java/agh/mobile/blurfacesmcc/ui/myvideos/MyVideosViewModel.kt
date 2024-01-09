package agh.mobile.blurfacesmcc.ui.myvideos

import agh.mobile.blurfacesmcc.ui.uploadvideo.getWorkInfo
import agh.mobile.blurfacesmcc.ui.util.videoDataStore
import agh.mobile.blurfacesmcc.workers.LocalBlurWorker
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyVideosViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val videoRecords = application.videoDataStore.data.mapLatest {
        it.objectsList
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val workInfos = getWorkInfo("localBlur")
        .mapLatest {
            it.associate { workInfo ->
                workInfo.id to workInfo.progress.getFloat(LocalBlurWorker.Progress, 0f)
            }
        }

    init {
        viewModelScope.launch {
            launch {
                workInfos.collect()
            }
            launch {
                videoRecords.collect()
            }
        }

    }

}