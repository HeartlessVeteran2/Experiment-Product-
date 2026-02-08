package com.heartless.experimentproduct.di

import com.heartless.experimentproduct.data.places.StubPlacesRepository
import com.heartless.experimentproduct.domain.repository.PlacesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing places-related dependencies.
 * 
 * Currently uses StubPlacesRepository for development/testing.
 * TODO: Switch to MapboxPlacesRepository when credentials are configured.
 */
@Module
@InstallIn(SingletonComponent::class)
object PlacesModule {

    /**
     * Provides singleton instance of PlacesRepository.
     * Currently uses stub implementation with mock data.
     */
    @Provides
    @Singleton
    fun providePlacesRepository(): PlacesRepository {
        return StubPlacesRepository()
    }
}
