package com.example.mydatabackupapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UploadedFileDao {

    // Get the last 10 uploaded files, sorted by upload time
    @Query("SELECT * FROM uploaded_files ORDER BY uploadedAt DESC LIMIT 10")
    suspend fun getLastTenFiles(): List<UploadedFileEntity>

    // Insert or replace file metadata
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: UploadedFileEntity)

    // Keep only the latest 10 files, delete older ones
    @Query("""
        DELETE FROM uploaded_files 
        WHERE documentId NOT IN (
            SELECT documentId FROM uploaded_files ORDER BY uploadedAt DESC LIMIT 10
        )
    """)
    suspend fun deleteOldFiles()
}
