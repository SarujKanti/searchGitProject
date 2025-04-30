package com.skd.githubsearch.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.skd.githubsearch.dataModel.GHRepo
import com.skd.githubsearch.repository.GitHubRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GitHubRepository(application)

    val repos: LiveData<List<GHRepo>> = repository.getCachedRepos()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Method to refresh repositories from API
    fun refreshRepos() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.fetchReposFromApi()
            _isLoading.value = false
        }
    }

    // Method to search for repositories locally
    fun search(query: String): LiveData<List<GHRepo>> = repository.searchLocal(query)
}
