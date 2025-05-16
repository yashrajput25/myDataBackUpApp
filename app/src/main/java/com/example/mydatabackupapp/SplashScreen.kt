package com.example.mydatabackupapp

import android.os.Handler
import android.os.Looper
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var animateToColor by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animateToColor = true
        Handler(Looper.getMainLooper()).postDelayed({ onTimeout() }, 3000)
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (animateToColor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
        animationSpec = androidx.compose.animation.core.tween(1000),
        label = "Splash background transition"
    )

    Surface(modifier = Modifier.fillMaxSize(), color = backgroundColor) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(120.dp))

            Text(
                text = "Data Store",
                fontSize = 36.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Text(
                text = "Powered by IIITD Students",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}
