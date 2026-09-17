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
        setForeground(createForegroundInfo("Starting download…"))

        val stepStates = steps.map { WorkerStepState(label = it.label, status = "PENDING") }.toMutableList()
        setProgress(buildData(stepStates))

        for (index in steps.indices) {
            stepStates[index] = stepStates[index].copy(status = "IN_PROGRESS")
            setProgress(buildData(stepStates))
            setForeground(createForegroundInfo("Downloading ${steps[index].label}…"))

            val result = steps[index].execute()

            stepStates[index] = when (result) {
                is SyncResult.Success ->
                    stepStates[index].copy(status = "SUCCESS", count = result.count)
                is SyncResult.Failure ->
                    stepStates[index].copy(status = "FAILURE", message = result.error.detail)
                is SyncResult.NetworkError ->
                    stepStates[index].copy(status = "FAILURE", message = "Could not contact the server")
            }
            setProgress(buildData(stepStates))

            if (stepStates[index].status == "FAILURE") {
                return Result.failure(buildData(stepStates))
            }
        }

        return Result.success(buildData(stepStates))
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