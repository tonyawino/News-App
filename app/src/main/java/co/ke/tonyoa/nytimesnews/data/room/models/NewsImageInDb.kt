package co.ke.tonyoa.nytimesnews.data.room.models

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

@Entity(
    tableName = "news_image",
    indices = [Index("id", unique = true), Index("news_id")],
    foreignKeys = [
        ForeignKey(
            parentColumns = ["id"],
            childColumns = ["news_id"],
            entity = NewsInDb::class,
            onDelete = CASCADE
        )
    ]
)
data class NewsImageInDb(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "news_id")
    val newsId: Long,
    @ColumnInfo(name = "caption")
    val caption: String,
    @ColumnInfo(name = "copyright")
    val copyright: String,
    @ColumnInfo(name = "url")
    val url: String
)