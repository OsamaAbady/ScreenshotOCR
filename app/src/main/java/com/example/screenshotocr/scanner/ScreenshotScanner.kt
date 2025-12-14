package com.example.screenshotocr.scanner

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.File

data class Screenshot(val id: Long, val path: String)

class ScreenshotScanner(private val context: Context) {

    companion object {
        private const val TAG = "ScreenshotScanner"
    }

    fun scan(): List<Screenshot> {
        val list = mutableListOf<Screenshot>()

        try {
            val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.RELATIVE_PATH
                )
            } else {
                arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA
                )
            }

            val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
            } else {
                "${MediaStore.Images.Media.DATA} LIKE ?"
            }

            val selectionArgs = arrayOf("%Screenshots%")

            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val displayNameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    val relativePathColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
                    
                    while (it.moveToNext()) {
                        val id = it.getLong(idColumn)
                        val displayName = it.getString(displayNameColumn)
                        val relativePath = it.getString(relativePathColumn)
                        
                        // Convert to file path for compatibility
                        val uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
                        val path = getPathFromUri(uri) ?: continue
                        
                        list.add(Screenshot(id, path))
                        Log.v(TAG, "Found screenshot: $displayName at $relativePath")
                    }
                } else {
                    val dataColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    
                    while (it.moveToNext()) {
                        val id = it.getLong(idColumn)
                        val path = it.getString(dataColumn)
                        
                        // Verify file exists
                        if (File(path).exists()) {
                            list.add(Screenshot(id, path))
                            Log.v(TAG, "Found screenshot: $path")
                        }
                    }
                }
            }
            
            Log.d(TAG, "Scanned ${list.size} screenshots")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning screenshots", e)
        }

        return list
    }

    private fun getPathFromUri(uri: Uri): String? {
        return try {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.Media.DATA),
                null,
                null,
                null
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    it.getString(columnIndex)
                } else null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not get path from URI: $uri", e)
            null
        }
    }
}