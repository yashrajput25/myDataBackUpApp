package com.example.mydatabackupapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun previewFile(context: Context, file: UploadedFile) {
    val uri = Uri.parse(file.url)

    when {
        // PDF files
        file.fileName.endsWith(".pdf", true) -> {
            val pdfIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            try {
                context.startActivity(pdfIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
            }
        }

        // Plain text files
        file.fileName.endsWith(".txt", true) -> {
            val intent = Intent(context, TextPreviewActivity::class.java)
            intent.putExtra("fileUrl", file.url)
            context.startActivity(intent)
        }

        // Fallback: Open in browser or system viewer
        else -> {
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

// Helper to infer MIME type from file extension
fun getMimeType(fileName: String): String {
    return when {
        fileName.endsWith(".pdf", true) -> "application/pdf"
        fileName.endsWith(".doc", true) || fileName.endsWith(".docx", true) -> "application/msword"
        fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
        fileName.endsWith(".png", true) -> "image/png"
        fileName.endsWith(".txt", true) -> "text/plain"
        else -> "*/*"
    }
}
