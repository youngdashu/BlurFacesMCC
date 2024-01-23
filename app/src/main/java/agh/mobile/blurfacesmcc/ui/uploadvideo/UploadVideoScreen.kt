package agh.mobile.blurfacesmcc.ui.uploadvideo

import agh.mobile.blurfacesmcc.R
import agh.mobile.blurfacesmcc.domain.RequestStatus
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
    navigateToMyVideos: () -> Unit,
    uploadVideoViewModel: UploadVideoViewModel = hiltViewModel(),
    faceCluster:FaceCluster= hiltViewModel(),
    showSnackbar: (String) -> Unit
) {
    val uploadStatus by uploadVideoViewModel.uploadStatus.collectAsState()
    val processingProgress by uploadVideoViewModel.processingProgress.collectAsState(initial = 0f)
    val errorMessage by uploadVideoViewModel.errorMessage.collectAsState(initial = null)

    val videoTitle by uploadVideoViewModel.videoTitle.collectAsState()

    val context = LocalContext.current

    var error by remember {
        mutableStateOf("")
    }


    faceCluster.postContext(context)

    var resultUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
//            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            val uri = data?.data ?: return@rememberLauncherForActivityResult
            resultUri = uri

            // this line breaks app xDD
//            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    val newIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
    newIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)



    val intent = ActivityResultContracts.GetContent().createIntent(context, "video/*")
        .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    val openDocumentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    resultUri = uri

                    // Handle the URI (e.g., open, read, or display the file)
                } catch (e: Exception) {
                    error = e.toString()
                }
            }
        }

    LaunchedEffect(null) {
        launcher.launch(intent)

//        openDocumentLauncher.launch(arrayOf("video/*"))

    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.uploadVideo)) },
                navigationIcon = {
                    IconButton(onClick = navigateToMyVideos) {
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
                .fillMaxSize()
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
                        verticalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        //Use Coil to display the selected image

                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .size(400.dp, 400.dp)
//                                .padding(16.dp)
                        )

                        OutlinedTextField(
                            value = videoTitle,
                            onValueChange = uploadVideoViewModel::updateVideoTitle,
                            isError = errorMessage != null,
                            supportingText = {
                                if (errorMessage != null) {
                                    Text(text = errorMessage!!)
                                }
                            },
                            label = {
                                Text(text = "Video name")
                            }
                        )

                        AnimatedVisibility(visible = videoTitle.isNotEmpty()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        enabled = uploadStatus != RequestStatus.WAITING,
                                        onClick = {
                                            uploadVideoViewModel.processIfVideoDoesNotExist(
                                                videoTitle,
                                                onSuccess = {
                                                    uploadVideoViewModel.extractFacesFromVideo(
                                                        resultUri!!
                                                    ) { message ->
                                                        navigateToMyVideos()
                                                        showSnackbar(
                                                            message ?: "Processing finished"
                                                        )
                                                    }
                                                },
                                                onFailure = {
                                                    showSnackbar("File already exists")
                                                    uploadVideoViewModel.updateErrorMessage("File already exists")
                                                }
                                            )
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
                                    uploadVideoViewModel.uploadVideoForProcessing(
                                        resultUri!!,
                                        videoTitle,
                                        navigateToMyVideos
                                    )
                                }) {
                                    Text(text = "Process online")
                                }

                                Button(onClick = {
                                    faceCluster.isTheSameFace(1, 1)
                                }) {
                                    Text(text = "Cluster")
                                }
                                Button(onClick = {
                                    uploadVideoViewModel.stressTest(
                                        resultUri!!,
                                        videoTitle
                                    )
                                }) {
                                    Text(text = "Stress test")
                                }
                            }
                        }
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                    if (error.isNotEmpty())
                        Text(text = error)
                }
            }
        }
    }


}