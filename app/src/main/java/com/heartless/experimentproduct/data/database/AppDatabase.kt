package com.heartless.experimentproduct.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room Database for the application.
 * Contains location pins and user preferences.
 */
@Database(
    entities = [LocationPinEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationPinDao(): LocationPinDao
}
