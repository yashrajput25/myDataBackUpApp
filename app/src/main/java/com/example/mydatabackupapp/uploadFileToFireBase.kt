package com.example.mydatabackupapp

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.Date

fun uploadFileToFireBase(
    context: Context,
    uri: Uri,
    fileNameInput: String,
    tagInput: String
) {
    val storage = Firebase.storage
    val storageRef = storage.reference
    val userId = Firebase.auth.currentUser?.uid ?: "anonymous"
    val fileName = "uploads/${userId}_${System.currentTimeMillis()}_${uri.lastPathSegment}"
    val fileRef = storageRef.child(fileName)

    val inputStream = context.contentResolver.openInputStream(uri)
    if (inputStream == null) {
        Toast.makeText(context, "❌ Could not open file stream", Toast.LENGTH_SHORT).show()
        return
    }

    val uploadTask = fileRef.putStream(inputStream)

    uploadTask
        .addOnProgressListener { taskSnapshot ->
            val totalBytes = taskSnapshot.totalByteCount.takeIf { it > 0 } ?: 1L
            val progress = (taskSnapshot.bytesTransferred.toFloat() / totalBytes * 100).toInt()
            // You can display progress using a snackbar, dialog, or notification if needed
        }
        .addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { url ->
                val mimeType = context.contentResolver.getType(uri)
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "pdf"
                val cleanName = fileNameInput.substringBeforeLast(".")
                val finalName = "$cleanName.$extension"

                val metadata = hashMapOf(
                    "fileName" to finalName,
                    "tags" to tagInput,
                    "uploadedAt" to Date(),  // ✅ Proper Firestore Timestamp // ✅ This stores it as Long

                    "url" to url.toString(),
                    "userId" to userId

                )

                Firebase.firestore.collection("uploadedFiles")
                    .add(metadata)
                    .addOnSuccessListener {
                        Toast.makeText(context, "✅ Upload successful", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "⚠️ File uploaded but metadata save failed", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener {
                Toast.makeText(context, "❌ Could not retrieve download URL", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "❌ Upload failed: ${it.message}", Toast.LENGTH_LONG).show()
        }
}
