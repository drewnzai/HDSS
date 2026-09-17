package com.andrew.hdss.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.andrew.hdss.network.SyncResult
import com.andrew.hdss.network.services.HouseholdApiService
import com.andrew.hdss.network.services.IndividualApiService
import com.andrew.hdss.network.services.LocationApiService
import com.andrew.hdss.network.services.MembershipApiService
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class WorkerStepState(
    val label: String,
    val status: String,
    val count: Int? = null,
    val message: String? = null
)

class DownloadDatabaseWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val locationApiService: LocationApiService,
    private val individualApiService: IndividualApiService,
    private val householdApiService: HouseholdApiService,
    private val membershipApiService: MembershipApiService
) : CoroutineWorker(appContext, workerParams) {

    private data class DownloadStep(
        val label: String,
        val execute: suspend () -> SyncResult
    )

    private val steps: List<DownloadStep> = listOf(
        DownloadStep(label = "Locations") { locationApiService.fetchLocations() },
        DownloadStep(label = "Individuals") { individualApiService.fetchIndividuals() },
        DownloadStep(label = "Households") { householdApiService.fetchHouseholds() },
        DownloadStep(label = "Memberships") { membershipApiService.fetchMemberships() }
    )

    override suspend fun doWork(): Result {
        ensureNotificationChannel()

        val stepStates = steps
            .map { WorkerStepState(label = it.label, status = "PENDING") }
            .toMutableList()

        updateProgress(stepStates)

        try {
            for (index in steps.indices) {
                val step = steps[index]

                stepStates[index] = stepStates[index].copy(
                    status = "IN_PROGRESS"
                )

                updateProgress(stepStates)
                updateNotification("Downloading ${step.label}…")

                try {
                    val result = step.execute()

                    when (result) {
                        is SyncResult.Success -> {
                            stepStates[index] = stepStates[index].copy(
                                status = "SUCCESS",
                                count = result.count
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

                            return Result.failure(buildData(stepStates))
                        }

                        is SyncResult.NetworkError -> {
                            stepStates[index] = stepStates[index].copy(
                                status = "FAILURE",
                                message = "Could not contact the server"
                            )

                            updateProgress(stepStates)

                            updateNotification(
                                "${step.label} failed: Could not contact the server",
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

                    return Result.failure(buildData(stepStates))
                }
            }

            updateNotification(
                "Database download complete",
                completed = true
            )

            return Result.success(buildData(stepStates))

        } catch (e: Exception) {
            updateNotification(
                "Database download failed",
                failed = true
            )

            return Result.failure(buildData(stepStates))
        }
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

    private fun buildData(stepStates: List<WorkerStepState>): Data =
        Data.Builder()
            .putString(KEY_STEPS_JSON, Json.encodeToString(stepStates))
            .build()

    private fun createForegroundInfo(contentText: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("HDSS Database Download")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        return ForegroundInfo(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

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
    }
}