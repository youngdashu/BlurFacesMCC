package agh.mobile.blurfacesmcc.ui.myvideos

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MyVideosScreen(
    viewModel: MyVideosViewModel = hiltViewModel()
) {
    val videos = viewModel.videoRecords.collectAsState()
    val externalStoragePermission = rememberPermissionState(
        Manifest.permission.READ_MEDIA_VIDEO
    )

    if (externalStoragePermission.status.isGranted) {
        videos.value?.let { videoRecords ->

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    Text(text = "Bluring in Progress", style = MaterialTheme.typography.titleLarge)
                    Text(
                        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                        text = "No videos in progress",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(text = "Blured Videos", style = MaterialTheme.typography.titleLarge)
                    videoRecords.map { videoRecord ->
                        MyVideoElement(
                            imageModifier = Modifier
                                .width(100.dp)
                                .height(100.dp),
                            fileName = videoRecord.filename,
                            uri = videoRecord.uri
                        )
                    }.ifEmpty {
                        Text(
                            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                            text = "No videos uploaded",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

            }
        } ?: CircularProgressIndicator()
    } else {
        LaunchedEffect(externalStoragePermission.status) {
            externalStoragePermission.launchPermissionRequest()
        }

    }

}