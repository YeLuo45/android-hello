package com.hello.android.data

import com.hello.android.data.local.dao.PostDao
import com.hello.android.data.local.entity.PostEntity
import com.hello.android.data.remote.ApiService
import com.hello.android.data.remote.Post
import com.hello.android.domain.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val postDao: PostDao,
    private val apiService: ApiService,
    private val logger: Logger
) {
    fun getCachedPosts(): Flow<List<Post>> = postDao.getAllPosts().map { entities ->
        entities.map { it.toPost() }
    }

    suspend fun refreshPosts(): Result<List<Post>> {
        return try {
            val posts = apiService.getPosts()
            val entities = posts.map { it.toEntity() }
            postDao.deleteAll()
            postDao.insertAll(entities)
            logger.log("Posts refreshed: ${posts.size} items")
            Result.success(posts)
        } catch (e: Exception) {
            logger.log("Failed to refresh posts: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getPost(id: Int): Post? {
        return postDao.getPostById(id)?.toPost()
    }

    suspend fun clearCache() {
        postDao.deleteAll()
        logger.log("Post cache cleared")
    }

    fun isCacheValid(maxAgeMillis: Long = CACHE_MAX_AGE_MILLIS): Boolean {
        val cachedPosts = postDao.getAllPostsOnce()
        if (cachedPosts.isEmpty()) return false
        val oldestCacheTime = cachedPosts.minOfOrNull { it.cachedAt } ?: return false
        return System.currentTimeMillis() - oldestCacheTime < maxAgeMillis
    }

    companion object {
        // Cache is considered valid for 15 minutes
        const val CACHE_MAX_AGE_MILLIS = 15 * 60 * 1000L
    }

    private fun PostEntity.toPost(): Post = Post(
        id = id,
        userId = userId,
        title = title,
        body = body
    )

    private fun Post.toEntity(): PostEntity = PostEntity(
        id = id,
        userId = userId,
        title = title,
        body = body
    )
}