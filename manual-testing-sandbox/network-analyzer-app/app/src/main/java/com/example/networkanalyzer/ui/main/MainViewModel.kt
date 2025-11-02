package com.example.networkanalyzer.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.example.networkanalyzer.data.ProxyState
import com.example.networkanalyzer.model.NetworkRequest
import com.example.networkanalyzer.repository.NetworkRepository

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NetworkRepository.getInstance(application)

    private val filterLiveData = MutableLiveData<String?>(null)
    val requests: LiveData<List<NetworkRequest>> = filterLiveData.switchMap { filter ->
        repository.observeRequests(filter)
    }

    private val _proxyState = MutableLiveData(ProxyState(isRunning = false))
    val proxyState: LiveData<ProxyState> = _proxyState

    fun setFilter(filter: String?) {
        filterLiveData.value = filter
    }

    fun updateProxyState(isRunning: Boolean, error: String? = null) {
        _proxyState.value = ProxyState(isRunning, error)
    }
}
