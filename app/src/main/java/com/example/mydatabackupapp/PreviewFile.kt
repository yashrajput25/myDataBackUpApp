package com.example.mydatabackupapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast



fun previewFile(context: Context, file: UploadedFile) {
    val uri = Uri.parse(file.url)

    when {
        file.fileName.endsWith(".pdf", true) -> {
            // Open PDF using Google Docs Viewer in WebView
            val intent = Intent(context, PdfPreviewActivity::class.java)
            intent.putExtra("pdfUrl", file.url)
            context.startActivity(intent)
        }

        file.fileName.endsWith(".txt", true) -> {
            // Open .txt file content in a new activity
            val intent = Intent(context, TextPreviewActivity::class.java)
            intent.putExtra("fileUrl", file.url)
            context.startActivity(intent)
        }

        else -> {
            // Fallback to general viewer
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, getMimeType(file.fileName))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "No app to preview this file", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


fun getMimeType(fileName: String): String? {
    return when {
        fileName.endsWith(".pdf", true) -> "application/pdf"
        fileName.endsWith(".doc", true) || fileName.endsWith(".docx", true) -> "application/msword"
        fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
        fileName.endsWith(".png", true) -> "image/png"
        else -> "*/*"
    }

}