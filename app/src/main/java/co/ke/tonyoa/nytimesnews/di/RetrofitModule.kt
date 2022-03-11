package co.ke.tonyoa.nytimesnews.di

import co.ke.tonyoa.nytimesnews.data.retrofit.utils.GsonDateDeSerializer
import co.ke.tonyoa.nytimesnews.data.retrofit.NyTimesApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {
    private const val BASE_URL = "https://api.nytimes.com/"

    @Singleton
    @Provides
    fun provideGson(dateDeSerializer: GsonDateDeSerializer): Gson {
        return GsonBuilder().registerTypeAdapter(Date::class.java, dateDeSerializer).create()
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideNyTimesApi(retrofit: Retrofit): NyTimesApi {
        return retrofit.create(NyTimesApi::class.java)
    }

}