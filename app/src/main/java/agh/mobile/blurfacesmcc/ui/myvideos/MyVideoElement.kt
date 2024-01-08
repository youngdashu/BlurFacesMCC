package agh.mobile.blurfacesmcc.ui.myvideos

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis

@Composable
fun MyVideoElement(
    fileName: String,
    uri: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    textColor: Color
) {
    val context = LocalContext.current
    LaunchedEffect(uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                Uri.parse(uri),
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    val model = runCatching {
        ImageRequest.Builder(context)
            .data(Uri.parse(uri))
            .allowHardware(false)
            .videoFrameMillis(0)
            .decoderFactory { result, options, _ -> VideoFrameDecoder(result.source, options) }
            .build()
    }.getOrNull()


    Row(
        modifier = Modifier
            .padding(top = 20.dp, bottom = 20.dp)
            .background(backgroundColor)
            .padding(5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        model?.let {
            AsyncImage(modifier = modifier, model = model, contentDescription = "")
        } ?: Icon(imageVector = Icons.Default.QuestionMark, contentDescription = "unknown")
        Text(text = fileName, color = textColor)
    }
}