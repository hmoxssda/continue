package com.example.networkanalyzer.util

import android.content.Context
import com.example.networkanalyzer.model.NetworkRequest
import com.example.networkanalyzer.repository.NetworkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object TrafficLogger {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile
    private var repository: NetworkRepository? = null

    fun init(context: Context) {
        if (repository == null) {
            repository = NetworkRepository.getInstance(context)
        }
    }

    fun logRequest(request: NetworkRequest) {
        val repo = repository ?: return
        scope.launch {
            repo.insertRequest(request)
        }
    }
}
