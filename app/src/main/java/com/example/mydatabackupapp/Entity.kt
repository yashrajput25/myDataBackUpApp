package com.example.mydatabackupapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "uploaded_files")
data class UploadedFileEntity(
    @PrimaryKey val documentId: String,
    val fileName: String,
    val url: String,
    val uploadedAt: Long
)
