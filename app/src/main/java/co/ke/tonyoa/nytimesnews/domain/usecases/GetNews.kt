package co.ke.tonyoa.nytimesnews.domain.usecases

import co.ke.tonyoa.nytimesnews.domain.models.News
import co.ke.tonyoa.nytimesnews.domain.models.OrderBy
import co.ke.tonyoa.nytimesnews.domain.repositories.NewsRepository
import co.ke.tonyoa.nytimesnews.utils.DataState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNews @Inject constructor(val newsRepository: NewsRepository) {

    operator fun invoke(query: String?, orderBy: OrderBy): Flow<DataState<Flow<List<News>>>> {
        return newsRepository.getNews(query, orderBy)
    }

}