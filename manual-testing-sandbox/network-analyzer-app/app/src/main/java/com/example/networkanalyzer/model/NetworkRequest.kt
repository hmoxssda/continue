package com.example.networkanalyzer.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_requests")
data class NetworkRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val method: String,
    val url: String,
    val requestHeaders: String,
    val requestBody: String?,
    val responseCode: Int?,
    val responseHeaders: String?,
    val responseBody: String?,
    val isHttps: Boolean,
    val durationMillis: Long,
    val notes: String? = null
)
