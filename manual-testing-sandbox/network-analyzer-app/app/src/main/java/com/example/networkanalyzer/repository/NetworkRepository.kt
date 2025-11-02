package com.example.networkanalyzer.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.networkanalyzer.db.NetworkRequestDao
import com.example.networkanalyzer.db.RequestDatabase
import com.example.networkanalyzer.model.NetworkRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NetworkRepository private constructor(private val requestDao: NetworkRequestDao) {

    fun observeRequests(filter: String?): LiveData<List<NetworkRequest>> {
        return if (filter.isNullOrBlank()) {
            requestDao.observeAll()
        } else {
            requestDao.observeFiltered(filter)
        }
    }

    suspend fun insertRequest(request: NetworkRequest) {
        withContext(Dispatchers.IO) {
            requestDao.insert(request)
        }
    }

    suspend fun findById(id: Long): NetworkRequest? {
        return withContext(Dispatchers.IO) {
            requestDao.findById(id)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: NetworkRepository? = null

        fun getInstance(context: Context): NetworkRepository {
            return INSTANCE ?: synchronized(this) {
                val dao = RequestDatabase.getInstance(context).requestDao()
                NetworkRepository(dao).also { INSTANCE = it }
            }
        }
    }
}
