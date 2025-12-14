package com.example.screenshotocr.util

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object HashUtil {
    
    private const val TAG = "HashUtil"
    private const val BUFFER_SIZE = 8192

    fun sha256(path: String): String {
        return try {
            val file = File(path)
            if (!file.exists()) {
                Log.w(TAG, "File does not exist: $path")
                return ""
            }

            val md = MessageDigest.getInstance("SHA-256")
            
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    md.update(buffer, 0, bytesRead)
                }
            }
            
            val digest = md.digest()
            digest.joinToString("") { "%02x".format(it) }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating hash for $path", e)
            // Return a fallback hash based on file path and size
            try {
                val file = File(path)
                "${path.hashCode()}_${file.length()}"
            } catch (e2: Exception) {
                path.hashCode().toString()
            }
        }
    }
}