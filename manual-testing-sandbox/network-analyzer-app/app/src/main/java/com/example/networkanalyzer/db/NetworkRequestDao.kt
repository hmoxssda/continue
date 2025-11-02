package com.example.networkanalyzer.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.networkanalyzer.model.NetworkRequest

@Dao
interface NetworkRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: NetworkRequest): Long

    @Query("SELECT * FROM network_requests ORDER BY timestamp DESC")
    fun observeAll(): LiveData<List<NetworkRequest>>

    @Query(
        "SELECT * FROM network_requests WHERE url LIKE '%' || :filter || '%' OR method LIKE '%' || :filter || '%' OR responseCode LIKE '%' || :filter || '%' ORDER BY timestamp DESC"
    )
    fun observeFiltered(filter: String): LiveData<List<NetworkRequest>>

    @Query("SELECT * FROM network_requests WHERE id = :id")
    suspend fun findById(id: Long): NetworkRequest?
}
