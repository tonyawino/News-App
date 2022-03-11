package co.ke.tonyoa.nytimesnews.data.room.joins

import androidx.room.Embedded
import androidx.room.Relation
import co.ke.tonyoa.nytimesnews.data.room.models.NewsImageInDb
import co.ke.tonyoa.nytimesnews.data.room.models.NewsInDb

data class NewsInDbWithNewsImagesInDb(
    @Embedded
    val newsInDb: NewsInDb,
    @Relation(parentColumn = "id", entityColumn = "news_id")
    val newsImageInDb: List<NewsImageInDb>
)