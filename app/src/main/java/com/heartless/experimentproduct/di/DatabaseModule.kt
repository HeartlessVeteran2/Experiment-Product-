package com.heartless.experimentproduct.di

import android.content.Context
import androidx.room.Room
import com.heartless.experimentproduct.data.database.AppDatabase
import com.heartless.experimentproduct.data.database.LocationPinDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing database-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "experiment_product_database"
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideLocationPinDao(database: AppDatabase): LocationPinDao {
        return database.locationPinDao()
    }
}
