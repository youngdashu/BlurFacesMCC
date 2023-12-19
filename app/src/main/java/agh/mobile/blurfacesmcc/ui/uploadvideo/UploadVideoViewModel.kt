package agh.mobile.blurfacesmcc.ui.uploadvideo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import androidx.core.net.toFile
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel
class UploadVideoViewModel @Inject constructor(
) : ViewModel() {
    fun dupa(context: Context, img_uri: Uri) {

        viewModelScope.launch(Dispatchers.Default) {
            val highAccuracyOpts = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()

            val retriever = MediaMetadataRetriever()
            try {
                println(img_uri.isAbsolute)
                retriever.setDataSource(context, img_uri)

                // Get the duration of the video in milliseconds
                val frames = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)?.toLong() ?: 0

                // Specify the interval to capture frames (e.g., every 1000 ms)
                val frameInterval = 30

                // Iterate over the duration of the video with the specified interval
                for (frame in 0 until frames step frameInterval.toLong()) {
                    println(frame)
                    // Get the frame at the current time
                    val frame = retriever.getFrameAtIndex(frame.toInt())

                    val img = InputImage.fromBitmap(frame!!, 0)

                    val detector = FaceDetection.getClient(highAccuracyOpts)
                    val result = detector.process(img)
                        .addOnSuccessListener { faces ->
                            println("dupa " + faces.size)
                            // Task completed successfully
                            // ...
                        }
                        .addOnFailureListener { e ->
                            // Task failed with an exception
                            // ...
                        }
                }
            } finally {
                retriever.release()
            }
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }
}