package com.example.screenshotocr.ocr

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.resume
import java.io.File

class OCRProcessor {

    private val recognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extract(context: Context, path: String): String {
        return try {
            // Verify file exists
            val file = File(path)
            if (!file.exists()) {
                Log.w("OCRProcessor", "File does not exist: $path")
                return ""
            }

            val image = InputImage.fromFilePath(context, Uri.parse("file://$path"))
            val result = recognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            Log.e("OCRProcessor", "Failed to extract text from $path", e)
            ""
        }
    }
}

// Extension function to convert Google Play Services Task to coroutine
suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        if (task.exception != null) {
            cont.resumeWithException(task.exception!!)
        } else {
            cont.resume(task.result)
        }
    }
}