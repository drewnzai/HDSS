package com.andrew.hdss

import android.app.Application
import androidx.work.Configuration
import com.andrew.hdss.container.HdssContainer
import com.andrew.hdss.worker.DownloadWorkerFactory

class HdssApplication: Application(), Configuration.Provider {
    lateinit var container: HdssContainer

    override fun onCreate() {
        super.onCreate()
        container = HdssContainer(applicationContext)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(DownloadWorkerFactory(container))
            .build()
}