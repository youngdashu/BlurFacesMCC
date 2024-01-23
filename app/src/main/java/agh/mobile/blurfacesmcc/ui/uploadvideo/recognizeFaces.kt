package agh.mobile.blurfacesmcc.ui.uploadvideo

import agh.mobile.blurfacesmcc.ConfidentialData
import agh.mobile.blurfacesmcc.ui.util.process.getFaceDetection
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.InputStream
import java.util.UUID
import kotlin.math.max


suspend fun recognize(
    context: Context,
    inputVideoUri: Uri,
    checkDensity: Float,
    confidentialData: List<ConfidentialData>,
): Boolean {

    val confidentialBitmaps = confidentialData.map {
        val inputStream: InputStream = context.contentResolver.openInputStream(Uri.parse(it.uri))!!
        BitmapFactory.decodeStream(inputStream)
    }

    val retriever = MediaMetadataRetriever()

    retriever.setDataSource(context, inputVideoUri)

    // Get the duration of the video in milliseconds
    val framesCount =
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
            ?.toInt() ?: 0

    val framesNumToCheck = (framesCount * checkDensity).toInt()

    val indices = (0..framesCount step (framesCount / framesNumToCheck).toInt()).toList()

    val faces = getFaceDetection(indices, context, inputVideoUri)

    val croppedFaces = faces.flatMap {
        val frame = it.frame
        val innerFaces = it.faces

        val croppedFaces = innerFaces.map {
            val rect = it.boundingBox

            val left = max(0, rect.left)
            var width = rect.width()
            if (left + width > frame.width){
                width = frame.width - left - 1
            }
            val top = max(0, rect.top)
            var height = rect.height()
            if (top + height > frame.height){
                height = frame.height - top - 1
            }

            val croppedFaceBitmap = Bitmap.createBitmap(frame, left, top, width, height)
            croppedFaceBitmap
        }
        croppedFaces
    }

    val result = confidentialBitmaps.map { bitmap ->
        PerformClustering.isSensitiveFaceInBitMapList(
            bitmap,
            croppedFaces.toMutableList(),
            (0..croppedFaces.size).map { "$it" }.toMutableList(),
            context
        )
    }.any()

    return result
}
