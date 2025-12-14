package com.example.screenshotocr

data class ImageResult(
    val filePath: String,
    val fileName: String,
    val extractedText: String,
    val thumbnail: String? = null
)