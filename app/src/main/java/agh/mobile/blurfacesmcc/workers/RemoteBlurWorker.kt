package agh.mobile.blurfacesmcc.workers

import agh.mobile.blurfacesmcc.domain.requestTypes.UploadVideoRequest
import agh.mobile.blurfacesmcc.repositories.interfaces.VideosRepository
import agh.mobile.blurfacesmcc.ui.util.process.updateVideoInDataStore
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.io.FileOutputStream
import java.net.URL


@HiltWorker
class RemoteBlurWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted parameters: WorkerParameters,
    private val videosRepository: VideosRepository
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        val rawUri = inputData.getString(URI_KEY)
        val videoTitle = inputData.getString(VIDEO_TITLE_KEY)!!

        try {
            val inputStream = context
                .contentResolver
                .openInputStream(Uri.parse(rawUri))!!
            val file = inputStream.readAllBytes()
            inputStream.close()
            val url = videosRepository.processRemote(UploadVideoRequest(file, videoTitle))
            val data = URL(url).readBytes()
            val outputDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                    .toString()
            val outputFile = File("$outputDir/$videoTitle.mp4")
            val stream = FileOutputStream(outputFile)
            stream.write(data)
            stream.close()
            context.updateVideoInDataStore(Uri.parse(rawUri)) {
                uri = outputFile.toUri().toString()
                blured = true
            }

            return Result.success()
        } catch (e: Exception) {
            context.updateVideoInDataStore(Uri.parse(rawUri)) {
                filename = "FAILED_$videoTitle"
                blured = true
            }
            return Result.failure(
                workDataOf("error" to e.message)
            )
        }
    }

    companion object {
        const val URI_KEY = "uri"
        const val VIDEO_TITLE_KEY = "videoTitle"
    }

}