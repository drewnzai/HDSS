package com.andrew.hdss.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.andrew.hdss.network.SyncResult
import com.andrew.hdss.network.services.FormApiService
import com.andrew.hdss.network.services.HouseholdApiService
import com.andrew.hdss.network.services.IndividualApiService
import com.andrew.hdss.network.services.LocationApiService
import com.andrew.hdss.network.services.MembershipApiService
import com.andrew.hdss.network.services.QuestionApiService
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class WorkerStepState(
    val label: String,
    val status: String,
    val count: Int? = null,
    val total: Int? = null,
    val downloaded: Int? = null,
    val message: String? = null
)

class DownloadDatabaseWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val locationApiService: LocationApiService,
    private val individualApiService: IndividualApiService,
    private val householdApiService: HouseholdApiService,
    private val membershipApiService: MembershipApiService,
    private val formApiService: FormApiService,
    private val questionApiService: QuestionApiService
) : CoroutineWorker(appContext, workerParams) {

    private data class DownloadStep(
        val label: String,
        val execute: suspend (
            onProgress: (suspend (downloaded: Int, total:Int) -> Unit)?
        ) -> SyncResult
    )

    private val steps: List<DownloadStep> = listOf(
        DownloadStep(label = "Locations") {
                _ ->
            locationApiService.fetchLocations()
        },
        DownloadStep(label = "Individuals") {
                onProgress ->
            individualApiService.fetchIndividuals(onProgress)
        },
        DownloadStep(label = "Households") {
                onProgress ->
            householdApiService.fetchHouseholds(onProgress)
        },
        DownloadStep(label = "Memberships") {
            onProgress ->
            membershipApiService.fetchMemberships(onProgress)
        },
        DownloadStep(label = "Forms"){
            onProgress ->
            formApiService.fetchForms(onProgress)
        },
        DownloadStep(label = "Questions"){
            _ ->
            questionApiService.getQuestionsByFormId()
        }
    )

    override suspend fun doWork(): Result {
        ensureNotificationChannel()

        val stepStates = steps
            .map { WorkerStepState(label = it.label, status = "PENDING") }
            .toMutableList()

        updateProgress(stepStates)

        for (index in steps.indices) {
            val step = steps[index]

            stepStates[index] = stepStates[index].copy(
                status = "IN_PROGRESS",
                downloaded = 0,
                total = null
            )

            updateProgress(stepStates)
            updateNotification("Downloading ${step.label}…")

            try {
                when (
                    val result = step.execute { downloaded, total ->

                        stepStates[index] = stepStates[index].copy(
                            status = "IN_PROGRESS",
                            downloaded = downloaded,
                            total = total
                        )

                        updateProgress(stepStates)

                        val percentage =
                            if (total > 0) {
                                downloaded * 100 / total
                            } else {
                                0
                            }

                        updateNotification(
                            "Downloading ${step.label}: $downloaded / $total ($percentage%)"
                        )
                    }
                ) {
                    is SyncResult.Success -> {
                        stepStates[index] = stepStates[index].copy(
                            status = "SUCCESS",
                            count = result.count,
                            downloaded = result.count,
                            total = result.count
                        )

                        updateProgress(stepStates)

                        updateNotification(
                            "${step.label} complete (${result.count})"
                        )
                    }

                    is SyncResult.Failure -> {
                        stepStates[index] = stepStates[index].copy(
                            status = "FAILURE",
                            message = result.error.detail
                        )

                        updateProgress(stepStates)

                        updateNotification(
                            "${step.label} failed: ${result.error.detail}",
                            failed = true
                        )

                        postFinalNotification(
                            "Database download failed at ${step.label}",
                            failed = true
                        )

                        return Result.failure(buildData(stepStates))
                    }

                    is SyncResult.Error -> {
                        stepStates[index] = stepStates[index].copy(
                            status = "FAILURE",
                            message = "Could not contact the server"
                        )

                        updateProgress(stepStates)

                        updateNotification(
                            "${step.label} failed: Could not contact the server",
                            failed = true
                        )

                        postFinalNotification(
                            "Database download failed at ${step.label}",
                            failed = true
                        )

                        return Result.failure(buildData(stepStates))
                    }
                }
            } catch (e: Exception) {
                stepStates[index] = stepStates[index].copy(
                    status = "FAILURE",
                    message = e.message ?: "Unexpected error"
                )

                updateProgress(stepStates)

                updateNotification(
                    "${step.label} failed",
                    failed = true
                )

                postFinalNotification(
                    "Database download failed at ${step.label}",
                    failed = true
                )

                return Result.failure(buildData(stepStates))
            }
        }

        updateNotification("Database download complete", completed = true)
        postFinalNotification("Database download complete", failed = false)
        return Result.success(buildData(stepStates))
    }

    private suspend fun updateProgress(
        stepStates: List<WorkerStepState>
    ) {
        setProgress(buildData(stepStates))
    }

    private suspend fun updateNotification(
        contentText: String,
        completed: Boolean = false,
        failed: Boolean = false
    ) {
        val notification = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setContentTitle("HDSS Database Download")
            .setContentText(contentText)
            .setSmallIcon(
                when {
                    failed -> android.R.drawable.stat_notify_error
                    completed -> android.R.drawable.stat_sys_download_done
                    else -> android.R.drawable.stat_sys_download
                }
            )
            .setOngoing(!completed && !failed)
            .setAutoCancel(completed || failed)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        setForeground(
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        )
    }

    private fun postFinalNotification(contentText: String, failed: Boolean) {
        if (ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("HDSS Database Download")
            .setContentText(contentText)
            .setSmallIcon(
                if (failed) android.R.drawable.stat_notify_error
                else android.R.drawable.stat_sys_download_done
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(FINAL_NOTIFICATION_ID, notification)
    }

    private fun buildData(stepStates: List<WorkerStepState>): Data =
        Data.Builder()
            .putString(KEY_STEPS_JSON, Json.encodeToString(stepStates))
            .build()

    private fun ensureNotificationChannel() {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Database Sync", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    companion object {
        const val WORK_NAME = "download_database"
        const val KEY_STEPS_JSON = "steps_json"
        const val CHANNEL_ID = "download_channel"
        private const val NOTIFICATION_ID = 1001
        private const val FINAL_NOTIFICATION_ID = 1002
    }
}