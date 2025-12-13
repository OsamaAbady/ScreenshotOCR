package com.example.screenshotocr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import android.content.Intent
import com.example.screenshotocr.worker.OCRWorker

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WorkManager.getInstance(this)
            .enqueueUniqueWork(
                "OCR_SCAN",
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<OCRWorker>().build()
            )

        startActivity(Intent(this, SearchActivity::class.java))
        finish()
    }
}