package com.example.mydatabackupapp

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast

fun downloadFile(context: Context, file: UploadedFile) {
    val request = DownloadManager.Request(Uri.parse(file.url))
        .setTitle(file.fileName)
        .setDescription("Downloading backup file")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.fileName)
        .setAllowedOverMetered(true)

    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    dm.enqueue(request)

    Toast.makeText(context, "⬇️ Download started...", Toast.LENGTH_SHORT).show()
}
