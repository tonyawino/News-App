package co.ke.tonyoa.nytimesnews.domain.repositories

import co.ke.tonyoa.nytimesnews.domain.models.News
import co.ke.tonyoa.nytimesnews.domain.models.OrderBy
import co.ke.tonyoa.nytimesnews.utils.DataState
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun getNews(query: String?, orderBy: OrderBy): Flow<DataState<Flow<List<News>>>>

    suspend fun getNewsById(id: Long): Flow<DataState<News?>>

    suspend fun createNews(news: News): Flow<DataState<News>>
}