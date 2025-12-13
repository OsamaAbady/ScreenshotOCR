package com.example.screenshotocr.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ScreenshotEntity(
    @PrimaryKey val id: Long,
    val path: String,
    val text: String,
    val hash: String
)