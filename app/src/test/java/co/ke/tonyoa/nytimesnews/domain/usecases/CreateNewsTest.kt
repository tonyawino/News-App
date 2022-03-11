package co.ke.tonyoa.nytimesnews.domain.usecases

import co.ke.tonyoa.nytimesnews.data.repositories.NewsRepositoryImpl
import co.ke.tonyoa.nytimesnews.domain.models.News
import co.ke.tonyoa.nytimesnews.utils.DataState
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
class CreateNewsTest {

    private lateinit var createNews: CreateNews
    private lateinit var newsRepositoryImpl: NewsRepositoryImpl

    @Before
    fun setup() {
        newsRepositoryImpl = mockk(relaxed = true)
        createNews = CreateNews(newsRepositoryImpl)

        coEvery { newsRepositoryImpl.createNews(any()) } returns flow { emit(DataState.Loading()) }
    }

    @Test
    fun `valid data calls repository create news`() = runBlockingTest {
        val news = News(
            0,
            "title",
            "abstract",
            Date(),
            "category",
            "author",
            "source",
            "url",
            emptyList()
        )
        createNews(
            news.title,
            news.newsAbstract,
            news.publishDate,
            news.category,
            news.author,
            news.source,
            news.url,
            news.images
        )
            .last()
        coVerify { newsRepositoryImpl.createNews(news) }
    }

    @Test
    fun `empty title returns failure`() = runBlockingTest {
        val last = createNews(
            "    ",
            "abstract",
            Date(),
            "category",
            "author",
            "source",
            "url",
            emptyList()
        ).last()
        assertThat(last).isInstanceOf(DataState.Failure::class.java)
    }

    @Test
    fun `empty news abstract returns failure`() = runBlockingTest {
        val last = createNews(
            "title",
            "    ",
            Date(),
            "category",
            "author",
            "source",
            "url",
            emptyList()
        ).last()
        assertThat(last).isInstanceOf(DataState.Failure::class.java)
    }

    @Test
    fun `empty category returns failure`() = runBlockingTest {
        val last = createNews(
            "title",
            "news abstract",
            Date(),
            "    ",
            "author",
            "source",
            "url",
            emptyList()
        ).last()
        assertThat(last).isInstanceOf(DataState.Failure::class.java)
    }

    @Test
    fun `empty author returns failure`() = runBlockingTest {
        val last = createNews(
            "title",
            "news abstract",
            Date(),
            "category",
            "    ",
            "source",
            "url",
            emptyList()
        ).last()
        assertThat(last).isInstanceOf(DataState.Failure::class.java)
    }

    @Test
    fun `empty source returns failure`() = runBlockingTest {
        val last = createNews(
            "title",
            "news abstract",
            Date(),
            "category",
            "Author",
            "    ",
            "url",
            emptyList()
        ).last()
        assertThat(last).isInstanceOf(DataState.Failure::class.java)
    }


    @Test
    fun `empty url returns failure`() = runBlockingTest {
        val last = createNews(
            "title",
            "news abstract",
            Date(),
            "category",
            "Author",
            "Source",
            "    ",
            emptyList()
        ).last()
        assertThat(last).isInstanceOf(DataState.Failure::class.java)
    }


}