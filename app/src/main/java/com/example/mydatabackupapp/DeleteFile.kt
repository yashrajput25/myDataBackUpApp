package com.example.mydatabackupapp

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

fun deleteFile(context: Context, file: UploadedFile) {
    val storageRef = Firebase.storage.getReferenceFromUrl(file.url)

    // Delete the file from Firebase Storage
    storageRef.delete()
        .addOnSuccessListener {
            // Now delete the metadata from Firestore
            Firebase.firestore.collection("uploadedFiles")
                .document(file.documentId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "🗑️ File deleted successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "⚠️ File removed but metadata not deleted", Toast.LENGTH_SHORT).show()
                }
        }
        .addOnFailureListener {
            Toast.makeText(context, "❌ Failed to delete file: ${it.message}", Toast.LENGTH_LONG).show()
        }
}
