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

//fun uploadFileToFireBase(
//    context: Context,
//    uri: Uri,
//    fileNameInput: String,
//    tagInput: String,
//
//    ) {
//    val storage = Firebase.storage
//    val storageRef = storage.reference
//    val userId = Firebase.auth.currentUser?.uid ?: "anonymous"
//    val fileName = "uploads/${userId}_${System.currentTimeMillis()}_${uri.lastPathSegment}"
//    val fileRef = storageRef.child(fileName)
//
//    val inputStream = context.contentResolver.openInputStream(uri)
//    if (inputStream == null) {
//        Toast.makeText(context, "❌ Failed to read file", Toast.LENGTH_SHORT).show()
//        return
//    }
//
//    val uploadTask = fileRef.putStream(inputStream)
//
//    uploadTask.addOnProgressListener { taskSnapshot ->
//        val totalBytes = taskSnapshot.totalByteCount.takeIf { it > 0 } ?: 1L
//        val progress = taskSnapshot.bytesTransferred.toFloat() / totalBytes.toFloat()
//
//    }
//
//    uploadTask
//        .addOnSuccessListener {
//            fileRef.downloadUrl.addOnSuccessListener { url ->
//                val metadata = hashMapOf(
//                    "fileName" to uri.lastPathSegment,
//                    "uploadedAt" to Date(),
//                    "url" to url.toString(),
//                    "userId" to Firebase.auth.currentUser?.uid
//                )
//
//                Firebase.firestore.collection("uploadedFiles")
//                    .add(metadata)
//                    .addOnSuccessListener {
//                        Toast.makeText(context, "✅ Uploaded", Toast.LENGTH_SHORT).show()
//                    }
//                    .addOnFailureListener {
//                        Toast.makeText(context, "❌ Failed to store metadata", Toast.LENGTH_SHORT).show()
//
//                    }
//            }.addOnFailureListener {
//                Toast.makeText(context, "❌ Failed to get download URL", Toast.LENGTH_SHORT).show()
//            }
//        }
//        .addOnFailureListener {
//            Toast.makeText(context, "❌ Upload failed", Toast.LENGTH_SHORT).show()
//        }
//}

fun uploadFileToFireBase(
    context: Context,
    uri: Uri,
    fileNameInput: String,
    tagInput: String,

    ) {
    val storage = Firebase.storage
    val storageRef = storage.reference
    val userId = Firebase.auth.currentUser?.uid ?: "anonymous"
    val fileName = "uploads/${userId}_${System.currentTimeMillis()}_${uri.lastPathSegment}"
    val fileRef = storageRef.child(fileName)

    val inputStream = context.contentResolver.openInputStream(uri)
    if (inputStream == null) {
        Toast.makeText(context, "❌ Failed to read file", Toast.LENGTH_SHORT).show()
        return
    }

    val uploadTask = fileRef.putStream(inputStream)

    uploadTask.addOnProgressListener { taskSnapshot ->
        val totalBytes = taskSnapshot.totalByteCount.takeIf { it > 0 } ?: 1L
        val progress = taskSnapshot.bytesTransferred.toFloat() / totalBytes.toFloat()

    }

    uploadTask
        .addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { url ->
                val mimeType = context.contentResolver.getType(uri)
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "pdf"
                val cleanedInput = fileNameInput.substringBeforeLast(".")
                val nameWithExtension = "$cleanedInput.$extension"
                val metadata = hashMapOf(
                    "fileName" to nameWithExtension ,
                    "tags" to tagInput,
                    "uploadedAt" to Date(),
                    "url" to url.toString(),
                    "userId" to Firebase.auth.currentUser?.uid
                )

                Firebase.firestore.collection("uploadedFiles")
                    .add(metadata)
                    .addOnSuccessListener {
                        Toast.makeText(context, "✅ Uploaded", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "❌ Failed to store metadata", Toast.LENGTH_SHORT).show()

                    }
            }.addOnFailureListener {
                Toast.makeText(context, "❌ Failed to get download URL", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "❌ Upload failed", Toast.LENGTH_SHORT).show()
        }
}