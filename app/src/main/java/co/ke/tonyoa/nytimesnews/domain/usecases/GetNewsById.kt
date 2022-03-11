package co.ke.tonyoa.nytimesnews.domain.usecases

import co.ke.tonyoa.nytimesnews.domain.models.News
import co.ke.tonyoa.nytimesnews.domain.repositories.NewsRepository
import co.ke.tonyoa.nytimesnews.utils.DataState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNewsById @Inject constructor(val newsRepository: NewsRepository) {

    suspend operator fun invoke(id: Long): Flow<DataState<News?>> {
        return newsRepository.getNewsById(id)
    }

}