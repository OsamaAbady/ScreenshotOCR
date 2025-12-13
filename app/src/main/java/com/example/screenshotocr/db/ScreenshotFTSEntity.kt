package com.example.screenshotocr.db

import androidx.room.Entity
import androidx.room.Fts5

@Fts5(contentEntity = ScreenshotEntity::class)
@Entity
data class ScreenshotFTSEntity(
    val text: String
)