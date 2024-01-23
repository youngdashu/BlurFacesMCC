package agh.mobile.blurfacesmcc.workers

import agh.mobile.blurfacesmcc.ui.util.MonitorBattery
import agh.mobile.blurfacesmcc.ui.util.confidentialDataArrayStore
import agh.mobile.blurfacesmcc.ui.util.process.processLocal
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.TimeSource


class LocalBlurWorker(
    private val context: Context,
    parameters: WorkerParameters
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        val rawUri = inputData.getString(URI_KEY)

        val videoUri = Uri.parse(Uri.decode(rawUri))

        val videoTitle = inputData.getString(VIDEO_TITLE_KEY)

        val notificationId = inputData.getInt(NOTIFICATION_ID_KEY, 1)

        setForeground(
            createForegroundInfo("Identifying faces 0%", notificationId)
        )

        val confidentialData = coroutineScope {
            async {
                context.confidentialDataArrayStore.data.first().objectsList
            }.await()
        } ?: emptyList()

        println("conf data")
        println(confidentialData)

        val monitorBattery = MonitorBattery(context)
        val batteryStart = monitorBattery.batteryLevel

        val timeSource = TimeSource.Monotonic
        val startTime = timeSource.markNow()

        val processingResult = processLocal(
            context,
            videoUri,
            videoTitle,
            confidentialData,
            id
        ) {
            coroutineScope {
                launch {
                    setForeground(
                        createForegroundInfo(
                            "Identifying faces ${(it * 100).roundToInt()}%",
                            notificationId
                        )
                    )
                }
                launch {
                    setProgress(workDataOf(Progress to it))
                }
            }
        }

        val endTime = timeSource.markNow()

        Log.d("time", "${endTime - startTime}")

        val batteryEnd = monitorBattery.batteryPct

        Log.d("xdd", "Baterry diff: ${batteryStart - batteryEnd}")

        return processingResult
            .getOrNull()?.let { Result.success() }
            ?: Result.failure(
                workDataOf("error" to processingResult.exceptionOrNull()?.message)
            )
    }

    private fun createForegroundInfo(progress: String, notificationId: Int): ForegroundInfo {
        val id = "blurNotification1"
        val title = "Blur progress"
        val cancel = "Cancel operation"
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(getId())

        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(android.R.drawable.star_on)
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()

        return ForegroundInfo(notificationId, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    }

    companion object {
        const val URI_KEY = "uri"
        const val VIDEO_TITLE_KEY = "videoTitle"
        const val NOTIFICATION_ID_KEY = "notificationId"
        const val Progress = "Progress"
    }

}