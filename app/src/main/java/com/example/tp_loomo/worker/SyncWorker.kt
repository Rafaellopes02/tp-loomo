package com.example.tp_loomo.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tp_loomo.data.repository.ProfileRepository

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = ProfileRepository(applicationContext)
        val success = repository.syncPendingUpdate()
        return if (success) Result.success() else Result.retry()
    }
}