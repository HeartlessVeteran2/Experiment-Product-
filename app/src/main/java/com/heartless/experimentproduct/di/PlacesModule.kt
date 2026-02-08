package com.heartless.experimentproduct.di

import android.content.Context
import com.heartless.experimentproduct.data.places.MapboxPlacesRepository
import com.heartless.experimentproduct.domain.repository.PlacesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing places-related dependencies.
 * Configures PlacesRepository with Mapbox Search SDK implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
object PlacesModule {

    /**
     * Provides singleton instance of PlacesRepository.
     * Uses Mapbox Search SDK implementation.
     */
    @Provides
    @Singleton
    fun providePlacesRepository(
        @ApplicationContext context: Context
    ): PlacesRepository {
        return MapboxPlacesRepository(context)
    }
}
