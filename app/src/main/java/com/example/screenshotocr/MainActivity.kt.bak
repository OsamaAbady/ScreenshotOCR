package com.example.screenshotocr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.example.screenshotocr.worker.OCRWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val OCR_WORK_NAME = "OCR_SCAN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "Starting Screenshot OCR application")
        
        // Start OCR processing in background
        startOCRProcessing()
        
        // Navigate to search after a brief delay to show the loading message
        lifecycleScope.launch {
            delay(1500) // Show loading message for 1.5 seconds
            navigateToSearch()
        }
    }

    private fun startOCRProcessing() {
        try {
            val workRequest = OneTimeWorkRequestBuilder<OCRWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(this)
                .enqueueUniqueWork(
                    OCR_WORK_NAME,
                    ExistingWorkPolicy.KEEP,
                    workRequest
                )

            Log.d(TAG, "OCR work enqueued successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start OCR processing", e)
            Toast.makeText(
                this,
                "Failed to start OCR processing: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun navigateToSearch() {
        try {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to navigate to search", e)
            Toast.makeText(
                this,
                "Failed to open search: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}