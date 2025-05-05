package com.example.mydatabackupapp
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.android.gms.common.internal.ImagesContract.URL
import java.net.URL

class TextPreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fileUrl = intent.getStringExtra("fileUrl")

        val textView = TextView(this)
        textView.setPadding(16, 16, 16, 16)

        fileUrl?.let { url ->
            Thread {
                try {
                    val content = URL(url).readText()
                    runOnUiThread {
                        textView.text = content
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        textView.text = "Error loading text: ${e.message}"
                    }
                }
            }.start()
        }

        setContentView(ScrollView(this).apply {
            addView(textView)
        })
    }
}
