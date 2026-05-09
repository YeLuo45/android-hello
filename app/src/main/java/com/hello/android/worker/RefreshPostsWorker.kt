package com.hello.android.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hello.android.data.PostRepository
import com.hello.android.data.local.dao.PostDao
import com.hello.android.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RefreshPostsWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val postRepository: PostRepository,
    private val postDao: PostDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "RefreshPostsWorker"
        const val WORK_NAME = "refresh_posts_periodic"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting posts refresh...")
        return try {
            // Get count of cached posts before refresh
            val oldPosts = postDao.getAllPostsOnce()
            val oldPostIds = oldPosts.map { it.id }.toSet()
            
            val result = postRepository.refreshPosts()
            
            result.fold(
                onSuccess = { newPosts ->
                    val newPostIds = newPosts.map { it.id }.toSet()
                    // Count truly new posts (in new but not in old)
                    val newPostCount = newPostIds.count { it !in oldPostIds }
                    
                    Log.d(TAG, "Successfully refreshed ${newPosts.size} posts")
                    
                    // Notify user about new posts if there are any new ones
                    if (newPostCount > 0) {
                        Log.d(TAG, "Detected $newPostCount new posts")
                        NotificationHelper.showNewPostsNotification(context, newPostCount)
                    } else {
                        // Just notify that refresh happened (no new content)
                        NotificationHelper.showNewPostsNotification(context, 0)
                    }
                    Result.success()
                },
                onFailure = { e ->
                    Log.e(TAG, "Failed to refresh posts: ${e.message}")
                    NotificationHelper.showNewPostsNotification(context, -1)
                    if (runAttemptCount < 3) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh posts: ${e.message}")
            NotificationHelper.showNewPostsNotification(context, -1)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
