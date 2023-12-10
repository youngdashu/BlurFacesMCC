package agh.mobile.blurfacesmcc.ui.myvideos

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis

@Composable
fun MyVideoElement(
    fileName: String,
    uri: String,
    imageModifier: Modifier = Modifier
) {
    val context = LocalContext.current
    context.contentResolver.takePersistableUriPermission(
        Uri.parse(uri),
        Intent.FLAG_GRANT_READ_URI_PERMISSION
    )


    val model = ImageRequest.Builder(context)
        .data(Uri.parse(uri))
        .allowHardware(false)
        .videoFrameMillis(0)
        .decoderFactory { result, options, _ -> VideoFrameDecoder(result.source, options) }
        .build()


    Row {
        AsyncImage(modifier = imageModifier, model = model, contentDescription = "")
        Text(text = fileName)

    }
}