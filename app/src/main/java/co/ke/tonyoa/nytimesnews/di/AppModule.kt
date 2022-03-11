package co.ke.tonyoa.nytimesnews.di

import android.content.Context
import co.ke.tonyoa.nytimesnews.data.repositories.NewsRepositoryImpl
import co.ke.tonyoa.nytimesnews.data.retrofit.NyTimesApi
import co.ke.tonyoa.nytimesnews.data.retrofit.RetrofitNewsMapper
import co.ke.tonyoa.nytimesnews.data.room.RoomNewsMapper
import co.ke.tonyoa.nytimesnews.data.room.daos.NewsDao
import co.ke.tonyoa.nytimesnews.data.room.daos.NewsImageDao
import co.ke.tonyoa.nytimesnews.domain.repositories.NewsRepository
import co.ke.tonyoa.nytimesnews.utils.AppDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun provideDispatchers(): AppDispatchers {
        return object : AppDispatchers {
            override fun main(): CoroutineDispatcher {
                return Dispatchers.Main
            }

            override fun default(): CoroutineDispatcher {
                return Dispatchers.Default
            }

            override fun io(): CoroutineDispatcher {
                return Dispatchers.IO
            }

        }
    }


    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    fun provideNewsRepository(
        retrofitNewsMapper: RetrofitNewsMapper, nyTimesApi: NyTimesApi,
        roomNewsMapper: RoomNewsMapper,
        newsDao: NewsDao, newsImageDao: NewsImageDao,
        appDispatchers: AppDispatchers, @ApplicationContext context: Context
    ): NewsRepository {
        return NewsRepositoryImpl(
            retrofitNewsMapper,
            nyTimesApi,
            roomNewsMapper,
            newsDao,
            newsImageDao,
            appDispatchers,
            context
        )
    }
}