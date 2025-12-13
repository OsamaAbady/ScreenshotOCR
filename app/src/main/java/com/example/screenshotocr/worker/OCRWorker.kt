package com.example.screenshotocr.worker

import android.content.Context
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

    override suspend fun doWork(): Result {
        val db = AppDatabase.get(applicationContext)
        val existing = db.dao().allHashes().toSet()

        val scanner = ScreenshotScanner(applicationContext)
        val ocr = OCRProcessor()

        scanner.scan().forEach {
            val hash = HashUtil.sha256(it.path)
            if (hash in existing) return@forEach

            val text = ocr.extract(applicationContext, it.path)
            db.dao().insert(
                ScreenshotEntity(it.id, it.path, text, hash)
            )
        }
        return Result.success()
    }
}