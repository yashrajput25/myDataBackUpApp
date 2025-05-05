package com.example.mydatabackupapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import deleteFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(onLogout: () -> Unit) {
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var fileList by remember { mutableStateOf<List<UploadedFile>>(emptyList()) }


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadFileToFireBase(
                context, it
            ){isLoading = it}
        }
    }

    // Initial load
// Live Firestore updates
    LaunchedEffect(Unit) {
        val currentUserId = Firebase.auth.currentUser?.uid

        Firebase.firestore.collection("uploadedFiles")
            .whereEqualTo("userId", currentUserId)
            .orderBy("uploadedAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val files = snapshot.mapNotNull { doc ->
                    val name = doc.getString("fileName")
                    val url = doc.getString("url")
                    val id = doc.id
                    if (name != null && url != null) UploadedFile(name, url, id) else null
                }
                fileList = files
            }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    val context = LocalContext.current
                    IconButton(onClick = {
                        Firebase.auth.signOut() // Sign out from Firebase
                        GoogleSignIn.getClient(
                            context,
                            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(context.getString(R.string.default_web_client_id))
                                .requestEmail()
                                .build()
                        ).signOut() // Sign out from Google
                        onLogout() // Trigger logout in MainActivity
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },

        content = { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Button(onClick = {
                    launcher.launch("*/*")
                }) {
                    Text(stringResource(R.string.select_file))
                }

                Spacer(modifier = Modifier.height(16.dp))

                var searchQuery by remember { mutableStateOf("") }
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search files") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
//uploaded_files
                fileList.filter {
                    it.fileName.contains(searchQuery, ignoreCase = true)
                }.forEach { file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { previewFile(context, file) }
                                .padding(vertical = 4.dp)
                        ) {
                            if (file.fileName.endsWith(".jpg", true) ||
                                file.fileName.endsWith(".jpeg", true) ||
                                file.fileName.endsWith(".png", true)
                            ) {
                                AsyncImage(
                                    model = file.url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .height(40.dp)
                                        .width(40.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Text(text = file.fileName)
                        }

                        IconButton(onClick = {
                            deleteFile(context, file) { isLoading = it } // ✅ Pass loading state handler
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }

                    }

                }
            }
        }
    )

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

}

fun previewFile(context: Context, file: UploadedFile) {
    val uri = Uri.parse(file.url)
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

fun getMimeType(fileName: String): String? {
    return when {
        fileName.endsWith(".pdf", true) -> "application/pdf"
        fileName.endsWith(".doc", true) || fileName.endsWith(".docx", true) -> "application/msword"
        fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
        fileName.endsWith(".png", true) -> "image/png"
        else -> "*/*"
    }

}
