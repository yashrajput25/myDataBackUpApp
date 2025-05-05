package com.example.mydatabackupapp

import android.os.Bundle
import androidx.activity.ComponentActivity

import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

data class UploadedFile(
    val fileName: String,
    val url: String,
    val documentId: String
)

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient


    @Suppress("DEPRECATION")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // from google-services.json
            .requestEmail()
            .build()

         googleSignInClient = GoogleSignIn.getClient(this, gso)


        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            MaterialTheme {
                var loggedIn by remember { mutableStateOf(Firebase.auth.currentUser != null) }

                if (loggedIn) {
                    BackupScreen(onLogout = { loggedIn = false })
                } else {
                    LoginScreen(
                        googleSignInClient = googleSignInClient,
                        onLoginSuccess = { loggedIn = true }
                    )
                }
            }
        }
    }
}



