package agh.mobile.blurfacesmcc.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModules {

    @Singleton
    @Provides
    fun providesRetrofit(): Retrofit =
        Retrofit.Builder().baseUrl("http://10.0.2.2:5000").build()
}