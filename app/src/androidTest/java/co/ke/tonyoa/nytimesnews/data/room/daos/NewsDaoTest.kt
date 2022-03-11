package co.ke.tonyoa.nytimesnews.data.room.daos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import co.ke.tonyoa.nytimesnews.CoroutineTestRule
import co.ke.tonyoa.nytimesnews.data.room.NewsDatabase
import co.ke.tonyoa.nytimesnews.data.room.RoomNewsMapper
import co.ke.tonyoa.nytimesnews.utils.Utils.createRandomNews
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
@SmallTest
class NewsDaoTest {

    @get: Rule
    val instantTaskExecutor = InstantTaskExecutorRule()

    @get: Rule(order = 0)
    val hiltAndroidRule = HiltAndroidRule(this)

    @get: Rule
    val coroutineTestRule = CoroutineTestRule()


    @Inject
    lateinit var newsDao: NewsDao

    @Inject
    lateinit var roomNewsMapper: RoomNewsMapper

    @Inject
    lateinit var newsDatabase: NewsDatabase

    @Before
    fun setUp() {
        hiltAndroidRule.inject()
    }

    @Test
    fun sortNewsByTitleDescending() = runBlockingTest {
        insertRandomNewsToDb()
        val job = launch {
            val last = newsDao.getAllNews("-title").last()
            for (value in 1 until last.size) {
                assertThat(last[value].newsInDb.title).isAtMost(last[value - 1].newsInDb.title)
            }
        }
        job.cancel()
    }

    @Test
    fun sortNewsByTitleAscending() = runBlockingTest {
        insertRandomNewsToDb()
        val job = launch {
            val last = newsDao.getAllNews("title").last()
            for (value in 1 until last.size) {
                assertThat(last[value].newsInDb.title).isAtLeast(last[value - 1].newsInDb.title)
            }
        }
        job.cancel()
    }

    @Test
    fun sortNewsByDateDescending() = runBlockingTest {
        insertRandomNewsToDb()
        val job = launch {
            val last = newsDao.getAllNews("-date").last()
            for (value in 1 until last.size) {
                assertThat(last[value].newsInDb.publishDate).isAtMost(last[value - 1].newsInDb.publishDate)
            }
        }
        job.cancel()
    }

    @Test
    fun sortNewsByDateAscending() = runBlockingTest {
        insertRandomNewsToDb()
        val job = launch {
            val last = newsDao.getAllNews("date").last()
            for (value in 1 until last.size) {
                assertThat(last[value].newsInDb.publishDate).isAtLeast(last[value - 1].newsInDb.publishDate)
            }
        }
        job.cancel()
    }

    @Test
    fun sortNewsByCategoryDescending() = runBlockingTest {
        insertRandomNewsToDb()
        val job = launch {
            val last = newsDao.getAllNews("-category").last()
            for (value in 1 until last.size) {
                assertThat(last[value].newsInDb.category).isAtMost(last[value - 1].newsInDb.category)
            }
        }
        job.cancel()
    }

    @Test
    fun sortNewsByCategoryAscending() = runBlockingTest {
        insertRandomNewsToDb()
        val job = launch {
            val last = newsDao.getAllNews("category").last()
            for (value in 1 until last.size) {
                assertThat(last[value].newsInDb.category).isAtLeast(last[value - 1].newsInDb.category)
            }
        }
        job.cancel()
    }

    @Test
    fun sortNewsBySourceDescending() = runBlockingTest {
        insertRandomNewsToDb()
        val job = launch {
            val last = newsDao.getAllNews("-source").last()
            for (value in 1 until last.size) {
                assertThat(last[value].newsInDb.source).isAtMost(last[value - 1].newsInDb.source)
            }
        }
        job.cancel()
    }

    @Test
    fun sortNewsBySourceAscending() = runBlockingTest {
        insertRandomNewsToDb()
        val job = launch {
            val last = newsDao.getAllNews("source").last()
            for (value in 1 until last.size) {
                assertThat(last[value].newsInDb.source).isAtLeast(last[value - 1].newsInDb.source)
            }
        }
        job.cancel()
    }

    @Test
    fun sortNewsByAuthorDescending() = runBlockingTest {
        insertRandomNewsToDb()
        val job = launch {
            val last = newsDao.getAllNews("-author").last()
            for (value in 1 until last.size) {
                assertThat(last[value].newsInDb.author).isAtMost(last[value - 1].newsInDb.author)
            }
        }
        job.cancel()
    }

    @Test
    fun sortNewsByAuthorAscending() = runBlockingTest {
        insertRandomNewsToDb()
        val job = launch {
            val last = newsDao.getAllNews("author").last()
            for (value in 1 until last.size) {
                assertThat(last[value].newsInDb.author).isAtLeast(last[value - 1].newsInDb.author)
            }
        }
        job.cancel()
    }

    @Test
    fun sortNewsByInvalid_sortsByIdAscending() = runBlockingTest {
        insertRandomNewsToDb()
        val job = launch {
            val last = newsDao.getAllNews("-").last()
            for (value in 1 until last.size) {
                assertThat(last[value].newsInDb.id).isAtLeast(last[value - 1].newsInDb.id)
            }
        }
        job.cancel()
    }

    @Test
    fun queryGetNews_filtersResults() = runBlockingTest {
        insertRandomNewsToDb()
        val queryString = "when"
        val job = launch {
            newsDao.insert(roomNewsMapper.domainToEntity(createRandomNews().copy(author = "$queryString other values")).newsInDb)
            val last = newsDao.getAllNewsWithQuery("%$queryString%", "").last()
            last.forEach { newsInDbWithImage ->
                val newsInDb = newsInDbWithImage.newsInDb
                assertThat(last.size).isGreaterThan(0)
                assertThat(
                    (
                            listOf(
                                newsInDb.author,
                                newsInDb.category,
                                newsInDb.newsAbstract,
                                newsInDb.source,
                                newsInDb.title
                            )
                                .joinToString { " " })
                )
                    .contains(queryString)
            }
        }
        job.cancel()
    }

    @Test
    fun queryGetNews_returnsEmptyList() = runBlockingTest {
        insertRandomNewsToDb()
        val queryString = "123456738516273hdbjcbhgeye//akjaknxte1!@$5"
        val job = launch {
            val last = newsDao.getAllNewsWithQuery("%$queryString%", "").last()
            assertThat(last.size).isEqualTo(0)
        }
        job.cancel()
    }

    @Test
    fun deleteNews_returnsEmptyList() = runBlockingTest {
        insertRandomNewsToDb()
        val job = launch {
            newsDao.deleteAllNews()
            val last = newsDao.getAllNews("").last()
            assertThat(last.size).isEqualTo(0)
        }
        job.cancel()
    }

    @Test
    fun getNewsWithCorrectId_returnsNews() = runBlockingTest {
        val newsId = 1L
        val insertedNewsInDb =
            roomNewsMapper.domainToEntity(createRandomNews().copy(id = newsId)).newsInDb
        val job = launch {
            newsDao.insert(insertedNewsInDb)
            val last = newsDao.getNewsById(newsId).last()
            val fetchedNews = last?.newsInDb
            assertThat(fetchedNews).isEqualTo(insertedNewsInDb)
        }
        job.cancel()
    }

    @Test
    fun getNewsWithWrongId_returnsNull() = runBlockingTest {
        val newsId = 1L
        val insertedNewsInDb =
            roomNewsMapper.domainToEntity(createRandomNews().copy(id = newsId)).newsInDb
        val job = launch {
            newsDao.insert(insertedNewsInDb)
            val last = newsDao.getNewsById(2).last()
            val fetchedNews = last?.newsInDb
            assertThat(fetchedNews).isNull()
        }
        job.cancel()
    }

    private fun insertRandomNewsToDb(count: Int = 50) {
        (1..count).map { createRandomNews() }
            .forEach { newsDao.insert(roomNewsMapper.domainToEntity(it).newsInDb) }
    }

    @After
    fun teardown() {
        if (newsDatabase.isOpen)
            newsDatabase.close()
    }

}