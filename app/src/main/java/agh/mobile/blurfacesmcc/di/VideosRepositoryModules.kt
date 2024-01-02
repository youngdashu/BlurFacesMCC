package agh.mobile.blurfacesmcc.di

import agh.mobile.blurfacesmcc.repositories.DefaultVideosRepository
import agh.mobile.blurfacesmcc.repositories.interfaces.VideosRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VideosRepositoryModules {

    @Singleton
    @Binds
    abstract fun providesVideoRepository(videosRepository: DefaultVideosRepository): VideosRepository
}