package com.example.networkanalyzer.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.networkanalyzer.model.NetworkRequest

@Database(entities = [NetworkRequest::class], version = 1, exportSchema = true)
abstract class RequestDatabase : RoomDatabase() {
    abstract fun requestDao(): NetworkRequestDao

    companion object {
        @Volatile
        private var INSTANCE: RequestDatabase? = null

        fun getInstance(context: Context): RequestDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RequestDatabase::class.java,
                    "network_requests.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
