package com.example.screenshotocr.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.screenshotocr.db.AppDatabase
import com.example.screenshotocr.db.ScreenshotEntity
import com.example.screenshotocr.ocr.OCRProcessor
import com.example.screenshotocr.scanner.ScreenshotScanner
import com.example.screenshotocr.util.HashUtil

class OCRWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "OCRWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting OCR processing...")
            
            val db = AppDatabase.get(applicationContext)
            val existing = db.dao().allHashes().toSet()
            Log.d(TAG, "Found ${existing.size} existing screenshots in database")

            val scanner = ScreenshotScanner(applicationContext)
            val screenshots = scanner.scan()
            Log.d(TAG, "Found ${screenshots.size} screenshots on device")

            val ocr = OCRProcessor()
            var processedCount = 0
            var errorCount = 0

            screenshots.forEach { screenshot ->
                try {
                    val hash = HashUtil.sha256(screenshot.path)
                    if (hash in existing) {
                        Log.v(TAG, "Skipping already processed screenshot: ${screenshot.path}")
                        return@forEach
                    }

                    Log.d(TAG, "Processing screenshot: ${screenshot.path}")
                    val text = ocr.extract(applicationContext, screenshot.path)
                    
                    if (text.isNotBlank()) {
                        db.dao().insert(
                            ScreenshotEntity(screenshot.id, screenshot.path, text, hash)
                        )
                        processedCount++
                        Log.d(TAG, "Successfully processed screenshot ${screenshot.id}")
                    } else {
                        Log.w(TAG, "No text extracted from screenshot: ${screenshot.path}")
                    }
                } catch (e: Exception) {
                    errorCount++
                    Log.e(TAG, "Failed to process screenshot: ${screenshot.path}", e)
                    // Continue processing other screenshots
                }
            }

            Log.i(TAG, "OCR processing completed. Processed: $processedCount, Errors: $errorCount")
            Result.success()
            
        } catch (e: Exception) {
            Log.e(TAG, "OCR worker failed", e)
            Result.failure()
        }
    }
}