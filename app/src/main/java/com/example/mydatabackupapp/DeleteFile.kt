import android.content.Context
import android.widget.Toast
import com.example.mydatabackupapp.UploadedFile
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

fun deleteFile(context: Context, file: UploadedFile, setLoading: (Boolean) -> Unit) {
    setLoading(true)

    val storage = Firebase.storage
    val fileRef = storage.getReferenceFromUrl(file.url)

    fileRef.delete().addOnSuccessListener {
        Firebase.firestore.collection("uploadedFiles")
            .document(file.documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "🗑️ File deleted!", Toast.LENGTH_SHORT).show()
                setLoading(false)
            }
            .addOnFailureListener {
                Toast.makeText(context, "❌ Failed to delete metadata", Toast.LENGTH_SHORT).show()
                setLoading(false)
            }
    }.addOnFailureListener {
        Toast.makeText(context, "❌ Failed to delete file: ${it.message}", Toast.LENGTH_LONG).show()
        setLoading(false)
    }
}
