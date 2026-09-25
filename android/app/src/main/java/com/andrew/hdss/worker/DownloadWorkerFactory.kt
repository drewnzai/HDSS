package com.andrew.hdss.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.andrew.hdss.container.HdssContainer

class DownloadWorkerFactory(
    private val container: HdssContainer
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            DownloadDatabaseWorker::class.java.name -> DownloadDatabaseWorker(
                appContext = appContext,
                workerParams = workerParameters,
                locationApiService = container.locationApiService,
                individualApiService = container.individualApiService,
                householdApiService = container.householdApiService,
                membershipApiService = container.membershipApiService,
                formApiService = container.formApiService,
                questionApiService = container.questionApiService,
                choiceApiService = container.choiceApiService
            )
            else -> null
        }
    }
}