package com.example.networkanalyzer.ui.variables

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import com.example.networkanalyzer.repository.NetworkRepository
import com.example.networkanalyzer.util.PostVariableAnalyzer
import kotlinx.coroutines.Dispatchers

class VariableExtractorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NetworkRepository.getInstance(application)

    fun analyzeRequest(id: Long) = liveData(Dispatchers.IO) {
        val request = repository.findById(id)
        if (request != null) {
            emit(PostVariableAnalyzer.analyze(request))
        } else {
            emit(null)
        }
    }
}
