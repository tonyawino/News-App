package co.ke.tonyoa.nytimesnews.di

import android.content.Context
import androidx.room.Room
import co.ke.tonyoa.nytimesnews.data.room.NewsDatabase
import co.ke.tonyoa.nytimesnews.data.room.daos.NewsDao
import co.ke.tonyoa.nytimesnews.data.room.daos.NewsImageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@TestInstallIn(components = [SingletonComponent::class], replaces = [RoomModule::class])
@Module
object TestRoomModule {

    @Singleton
    @Provides
    fun provideRoomDatabase(@ApplicationContext context: Context): NewsDatabase {
        return Room.inMemoryDatabaseBuilder(context, NewsDatabase::class.java)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideNewsDao(newsDatabase: NewsDatabase): NewsDao {
        return newsDatabase.newsDao
    }

    @Singleton
    @Provides
    fun provideNewsImageDao(newsDatabase: NewsDatabase): NewsImageDao {
        return newsDatabase.newsImageDao
    }
}