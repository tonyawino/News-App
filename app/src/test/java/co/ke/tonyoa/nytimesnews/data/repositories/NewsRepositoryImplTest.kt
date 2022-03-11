package co.ke.tonyoa.nytimesnews.data.repositories

import android.content.Context
import co.ke.tonyoa.nytimesnews.ApiResponses.failedResponse
import co.ke.tonyoa.nytimesnews.ApiResponses.successResponse
import co.ke.tonyoa.nytimesnews.ApiResponses.successResponseNullBody
import co.ke.tonyoa.nytimesnews.data.retrofit.NyTimesApi
import co.ke.tonyoa.nytimesnews.data.retrofit.RetrofitNewsMapper
import co.ke.tonyoa.nytimesnews.data.room.RoomNewsMapper
import co.ke.tonyoa.nytimesnews.data.room.daos.NewsDao
import co.ke.tonyoa.nytimesnews.data.room.daos.NewsImageDao
import co.ke.tonyoa.nytimesnews.data.room.joins.NewsInDbWithNewsImagesInDb
import co.ke.tonyoa.nytimesnews.data.room.models.NewsImageInDb
import co.ke.tonyoa.nytimesnews.data.room.models.NewsInDb
import co.ke.tonyoa.nytimesnews.domain.models.OrderBy
import co.ke.tonyoa.nytimesnews.utils.AppDispatchers
import co.ke.tonyoa.nytimesnews.utils.DataState
import co.ke.tonyoa.nytimesnews.utils.Utils.createRandomNews
import co.ke.tonyoa.nytimesnews.utils.isConnectedToInternet
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class NewsRepositoryImplTest {

    private val roomNewsMapper = RoomNewsMapper()
    private lateinit var newsRepositoryImpl: NewsRepositoryImpl
    private lateinit var nyTimesApi: NyTimesApi
    private lateinit var newsDao: NewsDao
    private lateinit var newsImageDao: NewsImageDao
    private lateinit var context: Context

    private val dispatchers: AppDispatchers = object : AppDispatchers {
        override fun main(): CoroutineDispatcher {
            return TestCoroutineDispatcher()
        }

        override fun default(): CoroutineDispatcher {
            return TestCoroutineDispatcher()
        }

        override fun io(): CoroutineDispatcher {
            return TestCoroutineDispatcher()
        }
    }

    @Before
    fun setup() {
        nyTimesApi = mockk(relaxed = true)
        newsDao = mockk(relaxed = true)
        newsImageDao = mockk(relaxed = true)
        context = mockk(relaxed = true)
        newsRepositoryImpl = NewsRepositoryImpl(
            RetrofitNewsMapper(),
            nyTimesApi,
            roomNewsMapper,
            newsDao,
            newsImageDao,
            dispatchers,
            context
        )

        coEvery { context.getString(any()) } returns "An Error Occurred"
        coEvery { context.isConnectedToInternet() } returns false
    }

    /**
     * getNews test cases
     * 1. Get News emits loading first
     * 2. Get news emits loading with data from local storage first
     * 3. The dao method queried could be
     *    a. getAllNewsWith query when query is not null
     *    b. getAllNews when query is null
     * 4. a. If connected to internet fetches from API
     *           i. If API response is successful and body is not null
     *           Emits Success
     *           Saves news items to database
     *           Saves news images to database
     *           ii. If an exception occurs or response body is null
     *           Emits failure
     *         b. If not connected emits Failure
     */

    @Test
    fun `get news starts in loading state`() = runBlockingTest {
        val first = newsRepositoryImpl.getNews("", OrderBy.Date()).first()
        assertThat(first).isInstanceOf(DataState.Loading::class.java)
    }

    @Test
    fun `second emit is loading state and returns data`() = runBlockingTest {
        every { newsDao.getAllNews(any()) } returns flow { getNewsInDbItems() }
        val second = newsRepositoryImpl.getNews("", OrderBy.Date()).drop(1).first()
        assertThat(second).isInstanceOf(DataState.Loading::class.java)
        assertThat((second as DataState.Loading).data).isNotNull()
    }

    @Test
    fun `query not null calls get news with query on db and not get all news`() = runBlockingTest {
        val query = "query"
        val orderBy = OrderBy.Date()
        newsRepositoryImpl.getNews(query, orderBy).collect()
        coVerify(exactly = 1) { newsDao.getAllNewsWithQuery(any(), any()) }
        coVerify { newsDao.getAllNews(any()) wasNot called }
    }

    @Test
    fun `query null calls get all news on db and not get news with query`() = runBlockingTest {
        val orderBy = OrderBy.Date()
        newsRepositoryImpl.getNews(null, orderBy).collect()
        coVerify(exactly = 1) { newsDao.getAllNews(any()) }
        coVerify { newsDao.getAllNewsWithQuery(any(), any()) wasNot called }
    }

    @Test
    fun `available network connection fetches from API`() = runBlockingTest {
        every { context.isConnectedToInternet() } returns true
        val orderBy = OrderBy.Date()
        newsRepositoryImpl.getNews(null, orderBy).collect()
        coVerify(exactly = 1) { nyTimesApi.getEmailedOrViewedNews() }
    }

    @Test
    fun `unavailable network connection does not fetch from API`() = runBlockingTest {
        every { context.isConnectedToInternet() } returns false
        val orderBy = OrderBy.Date()
        newsRepositoryImpl.getNews(null, orderBy).collect()
        coVerify { nyTimesApi.getEmailedOrViewedNews() wasNot called }
    }

    @Test
    fun `unavailable network returns failed state with data from local cache`() = runBlockingTest {
        every { context.isConnectedToInternet() } returns false
        every { newsDao.getAllNews(any()) } returns flow { getNewsInDbItems() }
        val last = newsRepositoryImpl.getNews("", OrderBy.Date()).last()
        assertThat(last).isInstanceOf(DataState.Failure::class.java)
        assertThat((last as DataState.Failure).data).isNotNull()
    }

    @Test
    fun `successful API request returns success`() = runBlockingTest {
        every { context.isConnectedToInternet() } returns true
        every { newsDao.getAllNews(any()) } returns flow { getNewsInDbItems() }
        coEvery { nyTimesApi.getEmailedOrViewedNews() } returns successResponse
        val last = newsRepositoryImpl.getNews("", OrderBy.Date()).last()
        assertThat(last).isInstanceOf(DataState.Success::class.java)
        assertThat((last as DataState.Success).data).isNotNull()
    }

    @Test
    fun `successful API request saves news items in database`() = runBlockingTest {
        every { context.isConnectedToInternet() } returns true
        coEvery { nyTimesApi.getEmailedOrViewedNews() } returns successResponse
        newsRepositoryImpl.getNews("", OrderBy.Date()).collect()
        coVerify(exactly = 1) { newsDao.upsert(any<List<NewsInDb>>()) }
    }


    @Test
    fun `successful API request saves news images in database`() = runBlockingTest {
        every { context.isConnectedToInternet() } returns true
        coEvery { nyTimesApi.getEmailedOrViewedNews() } returns successResponse
        newsRepositoryImpl.getNews("", OrderBy.Date()).collect()
        coVerify(exactly = 1) { newsImageDao.upsert(any<List<NewsImageInDb>>()) }
    }

    @Test
    fun `unsuccessful API request returns failure`() = runBlockingTest {
        every { context.isConnectedToInternet() } returns true
        every { newsDao.getAllNews(any()) } returns flow { getNewsInDbItems() }
        coEvery { nyTimesApi.getEmailedOrViewedNews() } returns failedResponse
        val last = newsRepositoryImpl.getNews("", OrderBy.Date()).last()
        assertThat(last).isInstanceOf(DataState.Failure::class.java)
        assertThat((last as DataState.Failure).data).isNotNull()
    }

    @Test
    fun `successful API request with null body returns failure`() = runBlockingTest {
        every { context.isConnectedToInternet() } returns true
        every { newsDao.getAllNews(any()) } returns flow { getNewsInDbItems() }
        coEvery { nyTimesApi.getEmailedOrViewedNews() } returns successResponseNullBody
        val last = newsRepositoryImpl.getNews("", OrderBy.Date()).last()
        assertThat(last).isInstanceOf(DataState.Failure::class.java)
        assertThat((last as DataState.Failure).data).isNotNull()
    }

    /**
     * getNewsById test cases
     * 1. First State is loading
     * 2. Get existing news returns success
     * 3. Get non-existing news returns failure
     */

    @Test
    fun `get news by id starts with loading with no news`() = runBlockingTest {
        val first = newsRepositoryImpl.getNewsById(1).first()
        assertThat(first).isInstanceOf(DataState.Loading::class.java)
        assertThat((first as DataState.Loading).data).isNull()
    }

    @Test
    fun `get an existing news returns success with the news`() = runBlockingTest {
        every { newsDao.getNewsById(any()) } returns getNewsInDbItems(1).asFlow()
        val last = newsRepositoryImpl.getNewsById(1).last()
        assertThat(last).isInstanceOf(DataState.Success::class.java)
        assertThat((last as DataState.Success).data).isNotNull()
    }

    @Test
    fun `get non existing news returns failure with no news`() = runBlockingTest {
        every { newsDao.getNewsById(any()) } returns flow { emit(null) }
        val last = newsRepositoryImpl.getNewsById(1).last()
        assertThat(last).isInstanceOf(DataState.Failure::class.java)
        assertThat((last as DataState.Failure).data).isNull()
    }

    /**
     * createNews test cases
     * 1. Starts with loading state and no data
     * 2. Saves single news item in database
     *      a. Failed save returns failure and the original news
     *      b. Successful save
     *          i. inserts images to database
     *          ii. Gets the news item from the database
     *          iii. If item exists return success with data
     *          iv. If fails return failure and original news
     *
     */

    @Test
    fun `create news starts with loading state`() = runBlockingTest {
        every { newsDao.getNewsById(any()) } returns flow { emit(null) }
        val first = newsRepositoryImpl.createNews(createRandomNews()).first()
        assertThat(first).isInstanceOf(DataState.Loading::class.java)
        assertThat((first as DataState.Loading).data).isNull()
    }

    @Test
    fun `create news inserts item to database`() = runBlockingTest {
        newsRepositoryImpl.createNews(createRandomNews()).collect()
        coVerify(exactly = 1) { newsDao.insert(any<NewsInDb>()) }
    }

    @Test
    fun `fail to create news returns failure with original news`() = runBlockingTest {
        coEvery { newsDao.insert(any<NewsInDb>()) } returns -1
        val news = createRandomNews()
        val last = newsRepositoryImpl.createNews(news).last()
        assertThat(last).isInstanceOf(DataState.Failure::class.java)
        assertThat((last as DataState.Failure).data).isEqualTo(news)
    }

    @Test
    fun `success to create news inserts images to database`() = runBlockingTest {
        coEvery { newsDao.insert(any<NewsInDb>()) } returns 5
        newsRepositoryImpl.createNews(createRandomNews()).collect()
        coVerify(exactly = 1) { newsImageDao.insert(any<List<NewsImageInDb>>()) }
    }

    @Test
    fun `success to create news gets new item from database`() = runBlockingTest {
        coEvery { newsDao.insert(any<NewsInDb>()) } returns 5
        newsRepositoryImpl.createNews(createRandomNews()).collect()
        coVerify(exactly = 1) { newsDao.getNewsById(any()) }
    }

    @Test
    fun `get item inserted from db returns success with the data`() = runBlockingTest {
        coEvery { newsDao.insert(any<NewsInDb>()) } returns 5
        coEvery { newsDao.getNewsById(any()) } returns getNewsInDbItems(1).asFlow()
        val news = createRandomNews()
        val last = newsRepositoryImpl.createNews(news).last()
        assertThat(last).isInstanceOf(DataState.Success::class.java)
        assertThat((last as DataState.Success).data).isNotEqualTo(news)
    }

    @Test
    fun `fail to get item inserted from db returns failure with the original data`() =
        runBlockingTest {
            coEvery { newsDao.insert(any<NewsInDb>()) } returns 5
            coEvery { newsDao.getNewsById(any()) } returns flow { emit(null) }
            val news = createRandomNews()
            val last = newsRepositoryImpl.createNews(news).last()
            assertThat(last).isInstanceOf(DataState.Failure::class.java)
            assertThat((last as DataState.Failure).data).isEqualTo(news)
        }

    private fun getNewsInDbItems(count: Int = 10): List<NewsInDbWithNewsImagesInDb> {
        return (1..count).map { roomNewsMapper.domainToEntity(createRandomNews()) }
    }
}