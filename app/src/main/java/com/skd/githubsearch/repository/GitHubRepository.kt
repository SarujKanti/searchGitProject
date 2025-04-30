package com.skd.githubsearch.repository

import android.content.Context
import com.skd.githubsearch.dataBase.AppDatabase
import com.skd.githubsearch.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GitHubRepository(context: Context) {
    private val dao = AppDatabase.getDatabase(context).repoDao()

    fun getCachedRepos() = dao.getAllRepos() // Get cached data

    fun searchLocal(query: String) = dao.searchRepos("%$query%") // Search in local database

    suspend fun fetchReposFromApi() {
        withContext(Dispatchers.IO) {
            val response = RetrofitClient.apiService.searchRepos()
            if (response.isSuccessful) {
                response.body()?.items?.let { dao.insertAll(it) }
            }
        }
    }
}