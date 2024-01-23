package agh.mobile.blurfacesmcc.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModules {

    @Singleton
    @Provides
    fun providesRetrofit(): Retrofit =
        Retrofit
            .Builder()
            //.baseUrl("http://blur-server.default.54.221.201.107.sslip.io")
            .baseUrl("http://blur-server.default.54.167.133.117.sslip.io")
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(200, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .writeTimeout(200, TimeUnit.SECONDS)
                    .callTimeout(Duration.ofSeconds(200))
                    .readTimeout(200, TimeUnit.SECONDS).build()
            )
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
}