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
import co.ke.tonyoa.nytimesnews.utils.isConnectedToInternet
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher

@ExperimentalCoroutinesApi
@TestInstallIn(components = [SingletonComponent::class], replaces = [AppModule::class])
@Module
object TestAppModule {

    @Provides
    fun provideContext(): Context {
        val context = mockk<Context>(relaxed = true)
        every { context.getString(any()) } returns "Some Error Message"
        every { context.isConnectedToInternet() } returns true
        return context
    }

    @Provides
    fun provideDispatchers(): AppDispatchers {
        return object : AppDispatchers {
            override fun main(): CoroutineDispatcher {
                return TestCoroutineDispatcher()
            }

            override fun default(): CoroutineDispatcher {
                return TestCoroutineDispatcher()
            }

            override fun io(): CoroutineDispatcher {
                return TestCoroutineDispatcher()
            }

        }
    }


    @ExperimentalCoroutinesApi
    @Provides
    fun provideNewsRepository(
        retrofitNewsMapper: RetrofitNewsMapper, nyTimesApi: NyTimesApi,
        roomNewsMapper: RoomNewsMapper,
        newsDao: NewsDao, newsImageDao: NewsImageDao,
        appDispatchers: AppDispatchers, context: Context
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