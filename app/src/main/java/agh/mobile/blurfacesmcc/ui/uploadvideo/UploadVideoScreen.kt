package agh.mobile.blurfacesmcc.ui.uploadvideo

import agh.mobile.blurfacesmcc.R
import agh.mobile.blurfacesmcc.domain.RequestStatus
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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
    uploadVideoViewModel: UploadVideoViewModel = hiltViewModel(),
    showSnackbar: (String) -> Unit
) {
    val uploadStatus by uploadVideoViewModel.uploadStatus.collectAsState()
    val processingProgress by uploadVideoViewModel.processingProgress.collectAsState(initial = 0f)

    val videoTitle by uploadVideoViewModel.videoTitle.collectAsState()

    val context = LocalContext.current

    var resultUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val flags = data?.flags!! and Intent.FLAG_GRANT_READ_URI_PERMISSION

            val uri = data.data ?: return@rememberLauncherForActivityResult
            resultUri = uri

            context.contentResolver.takePersistableUriPermission(uri, flags)
        }
    }

    val intent = ActivityResultContracts.GetContent().createIntent(context, "video/*")

    LaunchedEffect(null) {
        launcher.launch(intent)
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

            if (resultUri != null) {

                val painter = rememberAsyncImagePainter(
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(resultUri)
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
                            onValueChange = uploadVideoViewModel::updateVideoTitle,
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
                                    uploadVideoViewModel.extractFacesFromVideo(resultUri!!) { message ->
                                        navigateToHomePage()
                                        showSnackbar(message ?: "Processing finished")
                                    }

                                }
                            ) {
                                if (uploadStatus == RequestStatus.WAITING) {
                                    Text(text = "Processing in progress")
                                    CircularProgressIndicator(
                                        progress = processingProgress,
                                        modifier = Modifier
                                            .padding(start = 10.dp)
                                            .size(20.dp),
                                    )
                                } else {
                                    Text(text = stringResource(id = R.string.blur_faces))
                                }
                            }

                            OutlinedButton(
                                enabled = uploadStatus != RequestStatus.WAITING,
                                onClick = { resultUri = null }
                            ) {
                                Text(text = stringResource(id = R.string.cancel))
                            }
                        }
                        Button(onClick = {
                            uploadVideoViewModel.saveUploadedVideoURI(resultUri!!)
                            uploadVideoViewModel.uploadVideoForProcessing(
                                resultUri!!,
                                videoTitle,
                                navigateToHomePage
                            )
                        }) {
                            Text(text = "Process online")
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
                            launcher.launch(intent)
                        }
                    ) { Text(text = "Choose a media") }
                }
            }
        }
    }


}