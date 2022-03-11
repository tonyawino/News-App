package co.ke.tonyoa.nytimesnews.data.repositories

import android.content.Context
import co.ke.tonyoa.nytimesnews.R
import co.ke.tonyoa.nytimesnews.data.retrofit.NyTimesApi
import co.ke.tonyoa.nytimesnews.data.retrofit.RetrofitNewsMapper
import co.ke.tonyoa.nytimesnews.data.room.RoomNewsMapper
import co.ke.tonyoa.nytimesnews.data.room.daos.NewsDao
import co.ke.tonyoa.nytimesnews.data.room.daos.NewsImageDao
import co.ke.tonyoa.nytimesnews.data.room.joins.NewsInDbWithNewsImagesInDb
import co.ke.tonyoa.nytimesnews.data.room.models.NewsImageInDb
import co.ke.tonyoa.nytimesnews.data.room.models.NewsInDb
import co.ke.tonyoa.nytimesnews.domain.models.News
import co.ke.tonyoa.nytimesnews.domain.models.OrderBy
import co.ke.tonyoa.nytimesnews.domain.models.OrderDirection
import co.ke.tonyoa.nytimesnews.domain.repositories.NewsRepository
import co.ke.tonyoa.nytimesnews.utils.AppDispatchers
import co.ke.tonyoa.nytimesnews.utils.DataState
import co.ke.tonyoa.nytimesnews.utils.isConnectedToInternet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.net.ssl.SSLHandshakeException

@ExperimentalCoroutinesApi
class NewsRepositoryImpl @Inject constructor(
    private val retrofitNewsMapper: RetrofitNewsMapper,
    private val nyTimesApi: NyTimesApi,
    private val roomNewsMapper: RoomNewsMapper,
    private val newsDao: NewsDao,
    private val newsImageDao: NewsImageDao,
    private val dispatchers: AppDispatchers,
    private val context: Context
) : NewsRepository {

    override fun getNews(query: String?, orderBy: OrderBy): Flow<DataState<Flow<List<News>>>> =
        flow {
            // Display loading with no items
            emit(DataState.Loading())

            val sortBy = getSortBy(orderBy)

            val dbFlow: Flow<List<NewsInDbWithNewsImagesInDb>> =
                when {
                    (query != null) -> newsDao.getAllNewsWithQuery("%$query%", sortBy)
                        .distinctUntilChanged()
                    else -> newsDao.getAllNews(sortBy).distinctUntilChanged()
                }

            val newsItems = dbFlow.mapLatest {
                roomNewsMapper.entityListToDomainList(it)
            }

            // Display loading with current items
            emit(DataState.Loading(newsItems))

            var isSuccessful: Boolean
            var throwable: Throwable? = null
            val genericException = Exception(context.getString(R.string.geeric_error))
            val internetException = Exception(context.getString(R.string.internet_connection_error))
            if (context.isConnectedToInternet()) {
                try {
                    val viewedNews = nyTimesApi.getEmailedOrViewedNews()
                    val newsResponse = viewedNews.body()
                    // API call was successful
                    if (viewedNews.isSuccessful && newsResponse != null) {
                        isSuccessful = true
                        val domainModels =
                            retrofitNewsMapper.entityListToDomainList(newsResponse.newsItemRetros)
                        val roomModels = roomNewsMapper.domainListToEntityList(domainModels)

                        // Create lists of the items and images to insert at once
                        val newsModelItems = mutableListOf<NewsInDb>()
                        val newsImageModelItems = mutableListOf<NewsImageInDb>()
                        roomModels.forEach {
                            newsModelItems.add(it.newsInDb)
                            newsImageModelItems.addAll(it.newsImageInDb)
                        }
                        // Cache the data in local storage
                        newsDao.upsert(newsModelItems)
                        newsImageDao.upsert(newsImageModelItems)
                    } else if (viewedNews.code() == 429) {
                        isSuccessful = false
                        throwable = Exception(context.getString(R.string.too_many_requests))
                    } else if (viewedNews.errorBody() != null) {
                        isSuccessful = false
                        throwable = Exception(viewedNews.errorBody().toString())
                    } else {
                        isSuccessful = false
                        throwable = genericException
                    }
                } catch (e: SSLHandshakeException) {
                    isSuccessful = false
                    throwable = internetException
                } catch (e: Exception) {
                    isSuccessful = false
                    throwable = e
                }
            } else {
                isSuccessful = false
                throwable = internetException
            }

            // Emit result with current value in DB
            emit(
                if (isSuccessful) DataState.Success(newsItems) else DataState.Failure(
                    throwable ?: genericException, newsItems
                )
            )
        }.flowOn(dispatchers.io())

    override suspend fun getNewsById(id: Long): Flow<DataState<News?>> = channelFlow {
        send(DataState.Loading())
        newsDao.getNewsById(id).distinctUntilChanged().collectLatest {
            it?.let {
                send(DataState.Success(roomNewsMapper.entityToDomain(it)))
            } ?: send(
                DataState.Failure(
                    Exception(
                        context.getString(
                            R.string.news_with_id_not_found,
                            id.toInt()
                        )
                    )
                )
            )
        }
    }

    override suspend fun createNews(news: News): Flow<DataState<News>> = channelFlow {
        send(DataState.Loading())
        val newsWithImages = roomNewsMapper.domainToEntity(news)
        val newsId = newsDao.insert(newsWithImages.newsInDb)
        if (newsId < 1) {
            send(
                DataState.Failure(
                    Exception(context.getString(R.string.error_creating_news)),
                    news
                )
            )
        } else {
            val newsImages =
                newsWithImages.newsImageInDb.map { newsImage -> newsImage.copy(newsId = newsId) }
            newsImageDao.insert(newsImages)
            newsDao.getNewsById(newsId).distinctUntilChanged().collectLatest {
                it?.let { newsInDb ->
                    send(DataState.Success(roomNewsMapper.entityToDomain(newsInDb)))
                } ?: send(
                    DataState.Failure(
                        Exception(context.getString(R.string.error_creating_news)),
                        news
                    )
                )
            }
        }
    }

    private fun getSortBy(orderBy: OrderBy): String {
        val prefix = if (orderBy.orderDirection == OrderDirection.DESCENDING) "-" else ""
        val column = when (orderBy) {
            is OrderBy.Source -> "source"
            is OrderBy.Author -> "author"
            is OrderBy.Category -> "category"
            is OrderBy.Title -> "title"
            else -> "date"
        }
        return prefix + column
    }
}