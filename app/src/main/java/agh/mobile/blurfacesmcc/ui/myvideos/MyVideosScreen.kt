package agh.mobile.blurfacesmcc.ui.myvideos

import agh.mobile.blurfacesmcc.R
import agh.mobile.blurfacesmcc.VideoRecord
import agh.mobile.blurfacesmcc.ui.util.TopBar
import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MyVideosScreen(
    navigateBack: () -> Unit,
    viewModel: MyVideosViewModel = hiltViewModel()
) {
    val videos = viewModel.videoRecords.collectAsState()
    val externalStoragePermission = rememberPermissionState(
        Manifest.permission.READ_MEDIA_VIDEO
    )

    Scaffold(
        topBar = {
            TopBar(title = stringResource(id = R.string.myVideos)) {
                navigateBack()
            }
        }
    ) {
        if (externalStoragePermission.status.isGranted) {
            VideosContent(videos = videos.value, Modifier.padding(it))
        } else {
            LaunchedEffect(externalStoragePermission.status) {
                externalStoragePermission.launchPermissionRequest()
            }
        }
    }
}

@Composable
private fun VideosContent(
    videos: List<VideoRecord>?,
    modifier: Modifier = Modifier
) {
    videos?.let { videoRecords ->
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            userScrollEnabled = true
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp)
                ) {
                    Text(
                        text = "Bluring in Progress",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.wrapContentWidth(Alignment.Start)
                    )
                }
            }

            videoRecords.filter { !it.blured }.also {
                items(it) { videoRecord ->
                    Log.e("xdd", videoRecord.uri.toString())
                    MyVideoElement(
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp),
                        fileName = videoRecord.filename,
                        uri = videoRecord.uri,
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        textColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }.ifEmpty {
                item {
                    Text(
                        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                        text = "No videos in progress",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp)
                ) {
                    Text(
                        text = "Blured Videos",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.wrapContentWidth(Alignment.Start)
                    )
                }
            }

            videoRecords.filter { it.blured }.also {
                items(it) { videoRecord ->
                    MyVideoElement(
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp),
                        fileName = videoRecord.filename,
                        uri = videoRecord.uri,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        textColor = MaterialTheme.colorScheme.primary
                    )
                }
            }.ifEmpty {
                item {
                    Text(
                        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                        text = "No videos uploaded",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            }
    }

}