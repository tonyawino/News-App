package co.ke.tonyoa.nytimesnews.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import co.ke.tonyoa.nytimesnews.data.room.joins.NewsInDbWithNewsImagesInDb
import co.ke.tonyoa.nytimesnews.data.room.models.NewsInDb
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NewsDao : BaseDao<NewsInDb>() {
    companion object {
        private const val NEWS_ORDER_BY = " CASE WHEN :sortBy = 'title' THEN title END ASC, " +
                " CASE WHEN :sortBy = '-title' THEN title END DESC, " +
                " CASE WHEN :sortBy = 'date' THEN publish_date END ASC, " +
                " CASE WHEN :sortBy = '-date' THEN publish_date END DESC, " +
                " CASE WHEN :sortBy = 'category' THEN category END ASC, " +
                " CASE WHEN :sortBy = '-category' THEN category END DESC, " +
                " CASE WHEN :sortBy = 'source' THEN source END ASC, " +
                " CASE WHEN :sortBy = '-source' THEN source END DESC, " +
                " CASE WHEN :sortBy = 'author' THEN author END ASC, " +
                " CASE WHEN :sortBy = '-author' THEN author END DESC "
    }


    @Transaction
    @Query("SELECT * FROM news ORDER BY $NEWS_ORDER_BY")
    abstract fun getAllNews(sortBy: String): Flow<List<NewsInDbWithNewsImagesInDb>>

    @Transaction
    @Query(
        "SELECT * FROM news WHERE title LIKE :query OR news_abstract LIKE :query OR category LIKE :query OR " +
                " source LIKE :query OR author LIKE :query ORDER BY $NEWS_ORDER_BY"
    )
    abstract fun getAllNewsWithQuery(
        query: String,
        sortBy: String
    ): Flow<List<NewsInDbWithNewsImagesInDb>>

    @Query("DELETE FROM news")
    abstract fun deleteAllNews()

    @Transaction
    @Query("SELECT * FROM news WHERE id=:id")
    abstract fun getNewsById(id: Long): Flow<NewsInDbWithNewsImagesInDb?>

}