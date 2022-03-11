package co.ke.tonyoa.nytimesnews.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import co.ke.tonyoa.nytimesnews.data.room.models.NewsImageInDb

@Dao
abstract class NewsImageDao : BaseDao<NewsImageInDb>() {

    @Query("DELETE FROM news_image")
    abstract fun deleteAllNewsImages()
}