package com.example.mydatabackupapp

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UploadedFileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Provides access to DAO functions
    abstract fun uploadedFileDao(): UploadedFileDao
}
