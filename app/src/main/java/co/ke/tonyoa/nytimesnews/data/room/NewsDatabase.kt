package co.ke.tonyoa.nytimesnews.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import co.ke.tonyoa.nytimesnews.data.room.daos.NewsDao
import co.ke.tonyoa.nytimesnews.data.room.daos.NewsImageDao
import co.ke.tonyoa.nytimesnews.data.room.models.NewsImageInDb
import co.ke.tonyoa.nytimesnews.data.room.models.NewsInDb
import co.ke.tonyoa.nytimesnews.data.room.utils.DateConverter

@Database(entities = [NewsInDb::class, NewsImageInDb::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class NewsDatabase : RoomDatabase() {

    abstract val newsDao: NewsDao
    abstract val newsImageDao: NewsImageDao

    companion object {
        const val DATABASE_NAME = "news_db"
    }
}