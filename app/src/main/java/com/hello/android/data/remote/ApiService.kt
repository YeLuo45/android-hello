package com.hello.android.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
)

interface ApiService {
    @GET("posts")
    suspend fun getPosts(): List<Post>

    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Int): Post
}
