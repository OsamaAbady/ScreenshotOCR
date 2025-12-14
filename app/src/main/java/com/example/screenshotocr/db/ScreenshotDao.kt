package com.example.screenshotocr.db

import androidx.room.*

@Dao
interface ScreenshotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: ScreenshotEntity)

    @Query("""
        SELECT ScreenshotEntity.*
        FROM ScreenshotEntity
        JOIN ScreenshotFTSEntity
        ON ScreenshotEntity.id = ScreenshotFTSEntity.rowid
        WHERE ScreenshotFTSEntity MATCH :query
        ORDER BY ScreenshotEntity.id DESC
    """)
    fun search(query: String): List<ScreenshotEntity>

    @Query("SELECT hash FROM ScreenshotEntity")
    fun allHashes(): List<String>

    @Query("SELECT * FROM ScreenshotEntity ORDER BY id DESC")
    fun getAllScreenshots(): List<ScreenshotEntity>

    @Query("SELECT COUNT(*) FROM ScreenshotEntity")
    fun getScreenshotCount(): Int
}