package com.example.mydatabackupapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.example.mydatabackupapp.ui.theme.MyDataBackUpAppTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            var showSplash by remember { mutableStateOf(true) }
            var loggedIn by remember { mutableStateOf(Firebase.auth.currentUser != null) }

            MyDataBackUpAppTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (showSplash) {
                        SplashScreen { showSplash = false }
                    } else {
                        if (loggedIn) {
                            BackupScreen(
                                onLogout = { loggedIn = false },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme }
                            )
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
    }
}
