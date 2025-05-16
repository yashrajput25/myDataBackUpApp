package com.example.mydatabackupapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    var fileList by remember { mutableStateOf<List<UploadedFile>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var pickedFileUri by remember { mutableStateOf<Uri?>(null) }
    var fileNameInput by remember { mutableStateOf("") }
    var tagInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var sortByName by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

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

    // Real-time Firestore updates
    LaunchedEffect(Unit) {
        val userId = Firebase.auth.currentUser?.uid
        Log.d("FirestoreDebug", "Current Logged-in UID: $userId")
        Firebase.firestore.collection("uploadedFiles")
            .whereEqualTo("userId", userId)
//            .orderBy("uploadedAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                fileList = snapshot.mapNotNull { doc ->
                    val name = doc.getString("fileName")
                    val url = doc.getString("url")
                    val id = doc.id
                    val tags = doc.getString("tags")
                    val userIdFromDoc = doc.getString("userId")
                    Log.d("FirestoreDebug", "Document UID: $userIdFromDoc | File: $name")
                    if (name != null && url != null) UploadedFile(name, url, id, tags, userIdFromDoc) else null

                }

                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Store") },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        AnimatedContent(targetState = isDarkTheme) { targetState ->
                            Icon(
                                imageVector = if (targetState) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme"
                            )
                        }
                    }
                    IconButton(onClick = {
                        Firebase.auth.signOut()
                        GoogleSignIn.getClient(
                            context,
                            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(context.getString(R.string.default_web_client_id))
                                .requestEmail()
                                .build()
                        ).signOut()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                AnimatedVisibility(visible = true, enter = fadeIn()) {
                    Button(
                        onClick = { launcher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(stringResource(R.string.select_file))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.searchFiles)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.sort_by))
                    Button(
                        onClick = { sortByName = !sortByName },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(if (sortByName) stringResource(R.string.sort_name) else stringResource(R.string.sort_date))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    repeat(5) {
                        ShimmerEffect()
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    val displayedList = if (sortByName) {
                        fileList.sortedBy { it.fileName.lowercase() }
                    } else {
                        fileList
                    }

                    displayedList
                        .filter { it.fileName.contains(searchQuery, ignoreCase = true) }
                        .forEachIndexed { index, file ->
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically(initialOffsetY = { it * (index + 1) / 10 }) + fadeIn()
                            ) {
                                FileListItem(file = file, context = context)
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
                            uploadFileToFireBase(context, uri, fileNameInput, tagInput)
                        }
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Upload")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FileListItem(file: UploadedFile, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
                    contentDescription = "Thumbnail of ${file.fileName}",
                    modifier = Modifier.size(40.dp),
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
            IconButton(onClick = { deleteFile(context, file) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
fun shareFile(context: Context, file: UploadedFile) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Shared File: ${file.fileName}")
        putExtra(Intent.EXTRA_TEXT, "Here’s the file link: ${file.url}")
    }

    context.startActivity(
        Intent.createChooser(shareIntent, "Share File Via")
    )
}

@Composable
fun ShimmerEffect() {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 200f, translateAnim + 200f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(brush)
    )
}
