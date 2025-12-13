package com.example.screenshotocr.scanner

import android.content.Context
import android.provider.MediaStore

data class Screenshot(val id: Long, val path: String)

class ScreenshotScanner(private val context: Context) {

    fun scan(): List<Screenshot> {
        val list = mutableListOf<Screenshot>()

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA
            ),
            "${MediaStore.Images.Media.DATA} LIKE ?",
            arrayOf("%Screenshots%"),
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                list.add(
                    Screenshot(
                        it.getLong(0),
                        it.getString(1)
                    )
                )
            }
        }
        return list
    }
}