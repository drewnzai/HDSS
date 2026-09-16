package com.andrew.hdss.ui

sealed class DownloadStepStatus {
    data object Pending : DownloadStepStatus()
    data object InProgress : DownloadStepStatus()
    data class Success(val count: Int) : DownloadStepStatus()
    data class Failure(val message: String) : DownloadStepStatus()
}