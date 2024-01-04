package agh.mobile.blurfacesmcc.ui.uploadvideo

import agh.mobile.blurfacesmcc.VideoRecord
import agh.mobile.blurfacesmcc.domain.RequestStatus
import agh.mobile.blurfacesmcc.domain.requestTypes.UploadVideoRequest
import agh.mobile.blurfacesmcc.repositories.DefaultVideosRepository
import agh.mobile.blurfacesmcc.ui.util.videoDataStore
import android.R
import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.homesoft.encoder.FrameBuilder
import com.homesoft.encoder.Muxer
import com.homesoft.encoder.MuxerConfig
import com.homesoft.encoder.MuxingError
import com.homesoft.encoder.MuxingResult
import com.homesoft.encoder.MuxingSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.ArrayList
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.allOf
import javax.inject.Inject


@HiltViewModel
class UploadVideoViewModel @Inject constructor(
    private val application: Application,
    private val videosRepository: DefaultVideosRepository
) : AndroidViewModel(application) {
    fun extractFacesFromVideo(videoUri: Uri) {

        viewModelScope.launch(Dispatchers.Default) {
            val highAccuracyOpts = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()

            val retriever = MediaMetadataRetriever()
            try {
                println(videoUri.isAbsolute)
                retriever.setDataSource(getApplication(), videoUri)

                // Get the duration of the video in milliseconds
                val frames =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
                        ?.toLong() ?: 0

                // Specify the interval to capture frames (e.g., every 1000 ms)
                val frameInterval = 1

                val processingFutures = mutableListOf<CompletableFuture<Bitmap>>()
                val allTasksComplete = allOf(*processingFutures.toTypedArray())

                // Iterate over the duration of the video with the specified interval
                for (frame in 0 until frames step frameInterval.toLong()) {
                    val processingFuture = CompletableFuture<Bitmap>()
                    // Get the frame at the current time
                    val frame = retriever.getFrameAtIndex(frame.toInt())

                    val img = InputImage.fromBitmap(frame!!, 0)

                    val detector = FaceDetection.getClient(highAccuracyOpts)
                    detector.process(img)
                        .addOnSuccessListener { faces ->
                            Log.d("xdd", faces.size.toString())

                            val bitmapWithRectangles = drawRectanglesOnBitmap(frame, faces)
                            processingFuture.complete(bitmapWithRectangles)
                            // ...
                        }

                    processingFutures.add(processingFuture)
                }

                allTasksComplete.thenRun {
                    val processed_frames = processingFutures.map { it.join() }

                    println("dopa dupa dupa")

                    val outputdir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString()
                    val file = File("$outputdir/output.mp4")
                    val muxer = Muxer(application, MuxerConfig(
                        file,
                        retriever.getFrameAtIndex(0)!!.width,
                        retriever.getFrameAtIndex(0)!!.height,
                        framesPerSecond = 30F,
                        ))

                    println("mutex set")

                    aaa(processed_frames, muxer, file)

                    println("dupa dupa dupa")
                }
            } finally {
                retriever.release()
            }
        }
    }

    private fun aaa(imageList: List<Any>, muxer: Muxer, file: File): MuxingResult {
        Log.d("xd", "Generating video")
        val frameBuilder = FrameBuilder(application, muxer.getMuxerConfig(), null)

        try {
            frameBuilder.start()
        } catch (e: IOException) {
            Log.e("xdd", "Start Encoder Failed")
            e.printStackTrace()
            return MuxingError("Start encoder failed", e)
        }

        for (image in imageList) {
            frameBuilder.createFrame(image)
        }

        // Release the video codec so we can mux in the audio frames separately
        frameBuilder.releaseVideoCodec()

        frameBuilder.releaseMuxer()

        return MuxingSuccess(file)
    }

    private fun drawRectanglesOnBitmap(bitmap: Bitmap, faces: List<Face>): Bitmap {
        val resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)
        val paint = Paint()
        paint.color = ContextCompat.getColor(getApplication(), R.color.black)
        paint.style = Paint.Style.FILL

        for (face in faces) {
            val boundingBox = face.boundingBox
            canvas.drawRect(boundingBox, paint)
        }

        return resultBitmap
    }

    val uploadStatus = MutableStateFlow(RequestStatus.NOT_SEND)

    fun updateUploadStatus(newStatus: RequestStatus) {
        uploadStatus.update { newStatus }
    }

    fun uploadVideoForProcessing(uri: Uri, videoTitle: String, navigateToHomePage: () -> Unit) {
        viewModelScope.launch {
            updateUploadStatus(RequestStatus.WAITING)
            val inputStream = getApplication<Application>()
                .contentResolver
                .openInputStream(uri)!!
            val file = inputStream.readAllBytes()
            inputStream.close()
            videosRepository.processRemote(UploadVideoRequest(file, videoTitle))
        }.invokeOnCompletion {
            when (it?.cause) {
                is IOException -> {
                    Toast.makeText(getApplication(), "An error Occurred", Toast.LENGTH_SHORT).show()
                    updateUploadStatus(RequestStatus.NOT_SEND)
                }

                else -> {
                    if (it == null) {
                        Toast.makeText(
                            getApplication(),
                            "Video Uploaded Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        updateUploadStatus(RequestStatus.SUCCESS)
                        navigateToHomePage()
                    }
                }
            }
        }

    }

    fun saveUploadedVideoURI(uri: Uri) {
        viewModelScope.launch {
            getApplication<Application>().videoDataStore.updateData { videos ->
                getApplication<Application>()
                    .contentResolver
                    .query(uri, null, null, null, null)
                    .use { cursor ->
                        val nameIndex = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        cursor.moveToFirst()
                        videos.toBuilder().addObjects(
                            VideoRecord.getDefaultInstance().toBuilder()
                                .setUri(uri.toString())
                                .setFilename(cursor.getString(nameIndex))
                                .setBlured(true)
                        ).build()
                    }

            }
        }

    }
}