package com.example.screenshotocr.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.common.InputImage

class OCRProcessor {

    private val recognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extract(context: Context, path: String): String {
        val image = InputImage.fromFilePath(context, Uri.parse("file://$path"))
        return recognizer.process(image).await().text
    }
}

(You may need a simple Task → coroutine extension; your bot usually supports this)