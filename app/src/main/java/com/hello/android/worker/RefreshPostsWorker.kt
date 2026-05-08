package com.hello.android.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hello.android.data.remote.ApiService
import com.hello.android.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RefreshPostsWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: ApiService
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "RefreshPostsWorker"
        const val WORK_NAME = "refresh_posts_periodic"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting posts refresh...")
        return try {
            val posts = apiService.getPosts()
            Log.d(TAG, "Successfully refreshed ${posts.size} posts")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh posts: ${e.message}")
            // Notify user of failure
            NotificationHelper.showCounterNotification(
                context,
                -1 // Using -1 as a special marker for refresh failure
            )
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
