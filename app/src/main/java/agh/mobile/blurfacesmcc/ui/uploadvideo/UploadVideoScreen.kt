package agh.mobile.blurfacesmcc.ui.uploadvideo

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis


@Composable
fun UploadVideoScreen(
    uploadVideoViewModel: UploadVideoViewModel = hiltViewModel()
) {

    var result by remember {
        mutableStateOf<Uri?>(null)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) {
        result = it
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                launcher.launch(
                    PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
            }
        ) {
            Text(text = "Choose a media")
        }

        result?.let { image ->
            uploadVideoViewModel.saveUploadedPhotoURI(image)
            LocalContext.current.grantUriPermission(
                LocalContext.current.packageName,
                image,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            //Use Coil to display the selected image
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(image)
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
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(400.dp, 400.dp)
                    .padding(16.dp)
            )
        }


    }
}