package com.andrew.hdss

import android.app.Application
import com.andrew.hdss.container.HdssContainer

class HdssApplication: Application() {
    lateinit var container: HdssContainer

    override fun onCreate() {
        super.onCreate()
        container = HdssContainer(applicationContext)
    }
}