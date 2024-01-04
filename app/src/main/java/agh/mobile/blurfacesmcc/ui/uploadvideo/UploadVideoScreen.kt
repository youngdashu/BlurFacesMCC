package agh.mobile.blurfacesmcc.ui.uploadvideo

import agh.mobile.blurfacesmcc.R
import agh.mobile.blurfacesmcc.domain.RequestStatus
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadVideoScreen(
    navigateToHomePage: () -> Unit,
    uploadVideoViewModel: UploadVideoViewModel = hiltViewModel()
) {
    val uploadStatus by uploadVideoViewModel.uploadStatus.collectAsState()

    var result by remember {
        mutableStateOf<Uri?>(null)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) {
        result = it
    }

    LaunchedEffect(null) {
        launcher.launch(
            PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.VideoOnly)
        )
    }

    var videoTitle by remember {
        mutableStateOf("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.uploadVideo)) },
                navigationIcon = {
                    IconButton(onClick = navigateToHomePage) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(it)
                .fillMaxHeight()
        ) {

            if (result != null) {

                val painter = rememberAsyncImagePainter(
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(result)
                        .allowHardware(false)
                        .videoFrameMillis(0)
                        .decoderFactory { result, options, _ ->
                            VideoFrameDecoder(
                                result.source,
                                options
                            )
                        }
                        .build()
                )

                Card {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        //Use Coil to display the selected image

                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .size(400.dp, 400.dp)
                                .padding(16.dp)
                        )

                        OutlinedTextField(
                            value = videoTitle,
                            onValueChange = { videoTitle = it },
                            label = {
                                Text(text = "Video name")
                            }
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                enabled = uploadStatus != RequestStatus.WAITING,
                                onClick = {
                                    uploadVideoViewModel.extractFacesFromVideo(result!!)
//                                    uploadVideoViewModel.saveUploadedVideoURI(result!!)
//                                    uploadVideoViewModel.uploadVideoForProcessing(
//                                        result!!,
//                                        videoTitle,
//                                        navigateToHomePage
//                                    )
                                }
                            ) {
                                Text(text = stringResource(id = R.string.blur_faces))
                                if (uploadStatus == RequestStatus.WAITING) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .padding(start = 10.dp)
                                            .size(20.dp),
                                    )
                                }
                            }

                            OutlinedButton(
                                enabled = uploadStatus != RequestStatus.WAITING,
                                onClick = { result = null }
                            ) {
                                Text(text = stringResource(id = R.string.cancel))
                            }
                        }
                    }
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            launcher.launch(
                                PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        }
                    ) { Text(text = "Choose a media") }
                }
            }
        }
    }


}