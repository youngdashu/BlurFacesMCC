package agh.mobile.blurfacesmcc.ui.myvideos

import agh.mobile.blurfacesmcc.VideoRecord
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPasteGo
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    videoRecord: VideoRecord,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    textColor: Color
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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

    Card(
        modifier = Modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(5.dp)
        ) {
            model?.let {
                AsyncImage(modifier = modifier, model = model, contentDescription = "")
            } ?: Icon(imageVector = Icons.Default.QuestionMark, contentDescription = "unknown")
            Text(text = fileName, color = textColor)
            Column {
                Text(text = "Start: ${videoRecord.batteryStart}")

                if (videoRecord.batteryEnd > 0)
                    Text(text = "End: ${videoRecord.batteryEnd}")

//                Text(text = "Capacity: ${videoRecord.batteryCapacity}")

                if (videoRecord.batteryEnd > 0)
                    Text(text = "Diff: ${videoRecord.batteryStart - videoRecord.batteryEnd}")

                if (videoRecord.endTime.isNotEmpty()) {
                    Text(text = "Time: ${videoRecord.endTime}")
                }

                IconButton(onClick = {
                    val data = ClipData.newPlainText(
                        "power_lvl",
                        "start:${videoRecord.batteryStart}\nend:${videoRecord.batteryEnd}\ntime:${videoRecord.endTime}"

                    )
                    clipboard.setPrimaryClip(data)
                }) {
                    Icon(imageVector = Icons.Default.ContentPasteGo, contentDescription = "Paste")
                }

            }
        }
    }
}
