package com.example.mydatabackupapp

data class UploadedFile(
    val fileName: String,
    val url: String,
    val documentId: String,
    val tags: String? = null,
    val userId: String? = null
)
