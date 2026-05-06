package com.hello.android.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {

    fun schedulePeriodicPostRefresh(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val refreshRequest = PeriodicWorkRequestBuilder<RefreshPostsWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(RefreshPostsWorker.TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            RefreshPostsWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            refreshRequest
        )
    }

    fun cancelPeriodicPostRefresh(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(RefreshPostsWorker.WORK_NAME)
    }
}
