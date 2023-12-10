package agh.mobile.blurfacesmcc.ui.myvideos

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MyVideosScreen(
    viewModel: MyVideosViewModel = hiltViewModel()
) {
    val videos = viewModel.videoRecords.collectAsState()

    videos.value?.let { videoRecords ->
        videoRecords.map { videoRecord ->
            MyVideoElement(
                imageModifier = Modifier
                    .width(100.dp)
                    .height(100.dp),
                fileName = videoRecord.filename,
                uri = videoRecord.uri
            )
        }
    } ?: CircularProgressIndicator()
}