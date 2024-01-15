package agh.mobile.blurfacesmcc.ui.util.process

import agh.mobile.blurfacesmcc.VideoRecord
import agh.mobile.blurfacesmcc.ui.uploadvideo.FacesAtFrame
import agh.mobile.blurfacesmcc.ui.uploadvideo.PerformClustering
import agh.mobile.blurfacesmcc.ui.util.videoDataStore
import android.R
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.homesoft.encoder.FrameBuilder
import com.homesoft.encoder.Muxer
import com.homesoft.encoder.MuxerConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlin.math.roundToInt

suspend fun processLocal(
    context: Context,
    inputVideoUri: Uri,
    videoTitle: String?,
    jobId: UUID,
    reportProgress: suspend (Float) -> Unit
): Result<Unit> {
    val retriever = MediaMetadataRetriever()

    val outputDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString()

    val fileName = videoTitle.orEmpty().ifEmpty { "output" }

    val fileNameWithDir = "$outputDir/$fileName.mp4"
    val outputFile = File(fileNameWithDir)

    val result = runCatching {

        retriever.setDataSource(context, inputVideoUri)

        // Get the duration of the video in milliseconds
        val framesCount =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
                ?.toLong() ?: 0

        val listOfFrames = mutableListOf<Bitmap>()
        val listOfFramesNames = mutableListOf<String>()
        (0 until framesCount step 200).forEach{
            val bitMap = retriever.getFrameAtIndex(it.toInt())!!
            listOfFramesNames.add(it.toString())
            listOfFrames.add(bitMap)
        }

        PerformClustering.performClusteringForLinkedList(listOfFrames,listOfFramesNames,context)





        val fps = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
            ?.toFloatOrNull()

        context.saveVideoToDataStore {
            uri = outputFile.toUri().toString()
            filename = fileName
            blured = false
            this.jobId = jobId.toString()
        }

        val muxer = Muxer(
            context, MuxerConfig(
                outputFile,
                retriever.getFrameAtIndex(0)!!.width,
                retriever.getFrameAtIndex(0)!!.height,
                framesPerSecond = fps ?: 30F,
            )
        )
        val frameBitmapSizeMBytes =
            retriever.getFrameAtIndex(0)!!.allocationByteCount.toFloat() / 1000000F

        retriever.release()

        val chunkSize = (600F / frameBitmapSizeMBytes).roundToInt()

        println("Chunk size: $chunkSize")

        val frameIndices = (0..<framesCount.toInt()).chunked(chunkSize)

        val frameBuilder = FrameBuilder(context, muxer.getMuxerConfig(), null)

        try {
            frameBuilder.start()
        } catch (e: IOException) {
            Log.e("xdd", "Start Encoder Failed")
            e.printStackTrace()
        }

        coroutineScope {
            launch {
                frameIndices.forEachIndexed { i, indicesToProcess ->
                    val detectionResults =
                        getFaceDetection(indicesToProcess, context, inputVideoUri)
                    val result = getBitmaps(context, detectionResults)
                    for (image in result) {
                        frameBuilder.createFrame(image)
                    }

                    reportProgress(
                        (i + 1).toFloat() / frameIndices.size.toFloat()
                    )
                }
            }
        }.join()

        frameBuilder.releaseVideoCodec()
        frameBuilder.releaseMuxer()
        println("After Release")
    }.map {
        retriever.release()
    }.exceptionOrNull()

    val workResult = coroutineScope {
        async {
            println("Start async")
            if (result == null) {
                context.updateVideoInDataStore(outputFile.toUri()) {
                    println("Set blured")
                    this.blured = true
                }
                Result.success(Unit)
            } else {
                Log.e("error", result.stackTraceToString())
                File(fileNameWithDir).absoluteFile.delete()
                context.updateVideoInDataStore(inputVideoUri) {
                    filename = "FAILED $fileName"
                    blured = true
                }
                Result.failure(result)
            }
        }.await()
    }
    return workResult
}

private suspend fun Context.saveVideoToDataStore(builder: VideoRecord.Builder.() -> Unit) {
    videoDataStore.updateData { videos ->
        videos.toBuilder().addObjects(
            VideoRecord.getDefaultInstance().toBuilder().apply(builder)
        ).build()
    }
}

private suspend fun Context.updateVideoInDataStore(
    uri: Uri,
    updater: VideoRecord.Builder.() -> Unit
) {
    videoDataStore.updateData {
        val index = it.objectsList.indexOfFirst { videoRecord ->
            println(videoRecord.uri.toString())
            videoRecord.uri.toString() == uri.toString()
        }.takeIf { it > -1 } ?: return@updateData it
        val builder = it.getObjects(index).toBuilder().apply(updater)
        val videosBuilder = it.toBuilder().setObjects(index, builder)
        videosBuilder.build()
    }
}


private fun drawRectanglesOnBitmap(bitmap: Bitmap, faces: List<Face>, context: Context): Bitmap {
    val resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(resultBitmap)
    val paint = Paint()
    paint.color = ContextCompat.getColor(context, R.color.black)
    paint.style = Paint.Style.FILL

    for (face in faces) {
        val boundingBox = face.boundingBox
        canvas.drawRect(boundingBox, paint)
    }

    return resultBitmap
}


private fun createRetriever(
    context: Context,
    videoUri: Uri
): MediaMetadataRetriever {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(context, videoUri)
    return retriever
}

private suspend fun getFaceDetection(
    indicesToProcess: List<Int>,
    context: Context,
    videoUri: Uri
): List<FacesAtFrame> {

    val highAccuracyOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()
    val detector = FaceDetection.getClient(highAccuracyOpts)

    val retriever = createRetriever(context, videoUri)

    return try {
        coroutineScope {
            indicesToProcess.map { frameIndex ->
                async {
                    runCatching {
                        retriever.getFrameAtIndex(frameIndex)!!
                    }.getOrElse {
                        getAverageOfFrameNeighbors(frameIndex, retriever)
                    }?.let { frame ->
                        FacesAtFrame(detector.process(frame, 0).await(), frameIndex, frame)
                    }
                }
            }
        }.awaitAll().filterNotNull()
    } catch (e: Exception) {
        Log.d("xdd", e.toString())
        throw e
    } finally {
        retriever.release()
    }
}

private suspend fun getBitmaps(
    context: Context,
    detectionResults: List<FacesAtFrame>
): List<Bitmap> = coroutineScope {
    val bitmapsTasks = detectionResults.map { facesAtFrame ->
        this.async {
            val res = drawRectanglesOnBitmap(facesAtFrame.frame, facesAtFrame.faces, context)
            facesAtFrame.frame.recycle()
            res
        }
    }
    bitmapsTasks.awaitAll()
}


private fun getAverageOfFrameNeighbors(
    index: Int,
    retriever: MediaMetadataRetriever
): Bitmap? {
    val prevIndex = index - 1
    val nextIndex = index + 1
    println("Averaging $prevIndex $nextIndex")

    return runCatching {
        val prevFrame = retriever.getFrameAtIndex(prevIndex)!!
        val nextFrame = retriever.getFrameAtIndex(nextIndex)!!
        combineImages(prevFrame, nextFrame)
    }.onFailure { println("Averaging frames failed: ${it.message}") }.getOrNull()
}

private fun combineImages(bitmap1: Bitmap, bitmap2: Bitmap): Bitmap {
    val resultBitmap = Bitmap.createBitmap(bitmap1.width, bitmap1.height, bitmap1.config)

    val pixels1 = IntArray(bitmap1.width * bitmap1.height)
    val pixels2 = IntArray(bitmap2.width * bitmap2.height)

    bitmap1.getPixels(pixels1, 0, bitmap1.width, 0, 0, bitmap1.width, bitmap1.height)
    bitmap2.getPixels(pixels2, 0, bitmap2.width, 0, 0, bitmap2.width, bitmap2.height)

    val resultPixels = IntArray(pixels1.size)

    for (i in pixels1.indices) {
        val color1 = pixels1[i]
        val color2 = pixels2[i]

        // Extracting ARGB components
        val alpha1 = Color.alpha(color1)
        val red1 = Color.red(color1)
        val green1 = Color.green(color1)
        val blue1 = Color.blue(color1)

        val alpha2 = Color.alpha(color2)
        val red2 = Color.red(color2)
        val green2 = Color.green(color2)
        val blue2 = Color.blue(color2)

        // Calculating the average of each component
        val alphaAvg = (alpha1 + alpha2) / 2
        val redAvg = (red1 + red2) / 2
        val greenAvg = (green1 + green2) / 2
        val blueAvg = (blue1 + blue2) / 2

        // Combining components to create a new pixel
        resultPixels[i] = Color.argb(alphaAvg, redAvg, greenAvg, blueAvg)
    }

    resultBitmap.setPixels(resultPixels, 0, bitmap1.width, 0, 0, bitmap1.width, bitmap1.height)
    return resultBitmap
}