package com.skd.githubsearch.dataBase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.skd.githubsearch.dataModel.GHRepo

@Dao
interface RepoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(repos: List<GHRepo>)

    @Query("SELECT * FROM repos")
    fun getAllRepos(): LiveData<List<GHRepo>>

    @Query("SELECT * FROM repos WHERE name LIKE :query OR id LIKE :query")
    fun searchRepos(query: String): LiveData<List<GHRepo>>
}