package com.example.mydatabackupapp

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast

fun downloadFile(context: Context, file: UploadedFile) {
    val request = DownloadManager.Request(Uri.parse(file.url))
        .setTitle(file.fileName)
        .setDescription("Downloading backup file...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.fileName)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)

    Toast.makeText(context, "⬇️ Download started for ${file.fileName}", Toast.LENGTH_SHORT).show()
}
