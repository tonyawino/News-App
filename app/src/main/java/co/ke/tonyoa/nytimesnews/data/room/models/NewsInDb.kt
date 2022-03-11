package co.ke.tonyoa.nytimesnews.data.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "news", indices = [Index("id", unique = true), Index("title"),
        Index("publish_date"), Index("category"), Index("author"), Index("source")]
)
data class NewsInDb(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "news_abstract")
    val newsAbstract: String,
    @ColumnInfo(name = "publish_date")
    val publishDate: Date,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "author")
    val author: String,
    @ColumnInfo(name = "source")
    val source: String,
    @ColumnInfo(name = "url")
    val url: String
)