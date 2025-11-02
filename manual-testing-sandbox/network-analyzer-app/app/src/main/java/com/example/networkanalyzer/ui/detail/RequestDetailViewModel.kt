package com.example.networkanalyzer.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import com.example.networkanalyzer.repository.NetworkRepository
import kotlinx.coroutines.Dispatchers

class RequestDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NetworkRepository.getInstance(application)

    fun loadRequest(id: Long) = liveData(Dispatchers.IO) {
        emit(repository.findById(id))
    }
}
