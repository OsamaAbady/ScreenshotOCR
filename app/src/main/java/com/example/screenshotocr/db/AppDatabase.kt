package com.example.screenshotocr.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [ScreenshotEntity::class, ScreenshotFTSEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dao(): ScreenshotDao

    companion object {
        fun get(context: Context) =
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "screenshots.db"
            ).build()
    }
}