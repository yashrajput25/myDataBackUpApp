package com.example.mydatabackupapp

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun LoginScreen(
    context: Context = LocalContext.current,
    googleSignInClient: GoogleSignInClient,
    onLoginSuccess: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("SignInResult", "Result: $result")
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d("SignInResult", "Account email: ${account.email}, ID token: ${account.idToken}")

            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            Firebase.auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    Log.d("AuthSuccess", "Firebase login success for ${account.email}")
                    Toast.makeText(context, "✅ Welcome, ${account.displayName}", Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                }
                .addOnFailureListener {
                    Log.e("AuthError", "Firebase auth failed", it)
                    Toast.makeText(context, "❌ Auth failed: ${it.message}", Toast.LENGTH_LONG).show()
                }

        } catch (e: ApiException) {
            Log.e("SignInError", "Google Sign-In failed: ${e.message}")
            Toast.makeText(context, "❌ Google Sign-In failed", Toast.LENGTH_SHORT).show()
        }
    }


    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }) {
            Text(stringResource(R.string.signin_google))
        }
    }
}
