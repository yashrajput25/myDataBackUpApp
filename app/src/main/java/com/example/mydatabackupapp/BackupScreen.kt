package com.example.mydatabackupapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
    val context = LocalContext.current
    var fileList by remember { mutableStateOf<List<UploadedFile>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var pickedFileUri by remember { mutableStateOf<Uri?>(null) }
    var fileNameInput by remember { mutableStateOf("") }
    var tagInput by remember { mutableStateOf("") }


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            pickedFileUri = it
            fileNameInput = it.lastPathSegment ?: "uploaded_file"
            tagInput = ""
            showDialog = true
        }
    }


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

                Button(onClick = { launcher.launch("*/*") }) {
                    Text(stringResource(R.string.select_file))
                }


                Spacer(modifier = Modifier.height(16.dp))
                var searchQuery by remember { mutableStateOf("") }
                var sortByName by remember { mutableStateOf(false) }

                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search files") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.sort_by))
                    Button(onClick = { sortByName = !sortByName }) {
                        Text(if (sortByName) stringResource(R.string.sort_name) else stringResource(R.string.sort_date))
                    }
                }

                val sortedList = if (sortByName) {
                    fileList.sortedBy { it.fileName.lowercase() }
                } else {
                    fileList // already sorted by uploadedAt from Firestore
                }

                sortedList.filter{
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
                                .semantics {
                                    contentDescription = "Preview file ${file.fileName}"
                                }
                        ) {
                            if (file.fileName.endsWith(".jpg", true) ||
                                file.fileName.endsWith(".jpeg", true) ||
                                file.fileName.endsWith(".png", true)
                            ) {
                                AsyncImage(
                                    model = file.url,
                                    contentDescription = "Thumbnail of ${file.fileName}",
                                    modifier = Modifier
                                        .height(40.dp)
                                        .width(40.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Text(text = file.fileName)
                        }

                        Row {
                            IconButton(onClick = { shareFile(context, file) }) {
                                Icon(Icons.Default.Share, contentDescription = "Share")
                            }
                            Spacer(modifier = Modifier.width(8.dp)) // ← This adds space
                            IconButton(onClick = { deleteFile(context, file) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }

                    }

                }
            }
        }
    )
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Upload File") },
            text = {
                Column {
                    OutlinedTextField(
                        value = fileNameInput,
                        onValueChange = { fileNameInput = it },
                        label = { Text("File name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = { tagInput = it },
                        label = { Text("Tags (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pickedFileUri?.let { uri ->
                            uploadFileToFireBase(
                                context,
                                uri,
                                fileNameInput,
                                tagInput
                            )
                        }
                        showDialog = false
                    }
                ) {
                    Text("Upload")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }


}

fun shareFile(context: Context, file: UploadedFile) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Shared File: ${file.fileName}")
        putExtra(Intent.EXTRA_TEXT, "Download this file: ${file.url}")
    }

    val chooser = Intent.createChooser(intent, "Share File via")
    context.startActivity(chooser)
}
