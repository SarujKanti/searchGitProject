package com.skd.githubsearch.network

import com.skd.githubsearch.dataModel.GHRepo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class RepoResponse(val items: List<GHRepo>)

interface ApiService {
    @GET("search/repositories")
    suspend fun searchRepos(
        @Query("q") query: String = "language:swift",
        @Query("sort") sort: String = "stars",
        @Query("order") order: String = "desc"
    ): Response<RepoResponse>
}
