package co.ke.tonyoa.nytimesnews.ui.list

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import co.ke.tonyoa.nytimesnews.CoroutineTestRule
import co.ke.tonyoa.nytimesnews.domain.models.News
import co.ke.tonyoa.nytimesnews.domain.models.NewsImage
import co.ke.tonyoa.nytimesnews.domain.models.OrderBy
import co.ke.tonyoa.nytimesnews.domain.models.OrderDirection
import co.ke.tonyoa.nytimesnews.domain.usecases.CreateNews
import co.ke.tonyoa.nytimesnews.domain.usecases.GetNews
import co.ke.tonyoa.nytimesnews.getOrAwaitValue
import co.ke.tonyoa.nytimesnews.utils.DataState
import co.ke.tonyoa.nytimesnews.utils.Utils.createRandomNews
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.Exception
import java.util.*

@ExperimentalCoroutinesApi
@FlowPreview
class ItemListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var getNews: GetNews
    private lateinit var createNews: CreateNews
    private lateinit var itemListViewModel: ItemListViewModel

    @Before
    fun setup() {
        getNews = mockk(relaxed = true)
        createNews = mockk(relaxed = true)
        itemListViewModel = ItemListViewModel(getNews, createNews)
    }

    @Test
    fun `initializing item list viewmodel fetches data`() = runBlockingTest {
        verify(exactly = 1) { getNews(any(), any()) }
    }

    @Test
    fun `search with blank query changes query in state to null`() = runBlockingTest {
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Search("      "))
        val value = itemListViewModel.listUiState.getOrAwaitValue()
        assertThat(value.query).isNull()
    }

    @Test
    fun `search with new query changes query in state to that value`() = runBlockingTest {
        val newQuery = "some query"
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Search(newQuery))
        val value = itemListViewModel.listUiState.getOrAwaitValue()
        assertThat(value.query).isEqualTo(newQuery)
    }

    @Test
    fun `search with new query performs a search`() = runBlockingTest {
        val newQuery = "some query"
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Search(newQuery))
        verify(atLeast = 2) { getNews(any(), any()) }
    }

    @Test
    fun `search with same query multiple times performs query once`() = runBlockingTest {
        val newQuery = "some query"
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Search(newQuery))
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Search(newQuery))
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Search(newQuery))
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Search(newQuery))
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Search(newQuery))
        // First call is at initialization, second call when query is passed first time
        verify(exactly = 2) { getNews(any(), any()) }
    }

    @Test
    fun `sort ascending changes direction of orderby in state to ascending`() = runBlockingTest {
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Sort("date", true))
        val value = itemListViewModel.listUiState.getOrAwaitValue()
        assertThat(value.orderBy.orderDirection).isEqualTo(OrderDirection.ASCENDING)
    }

    @Test
    fun `sort descending changes direction of orderby in state to descending`() = runBlockingTest {
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Sort("date", false))
        val value = itemListViewModel.listUiState.getOrAwaitValue()
        assertThat(value.orderBy.orderDirection).isEqualTo(OrderDirection.DESCENDING)
    }

    @Test
    fun `sort by title changes orderby in state to title`() = runBlockingTest {
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Sort("title", false))
        val value = itemListViewModel.listUiState.getOrAwaitValue()
        assertThat(value.orderBy).isInstanceOf(OrderBy.Title::class.java)
    }

    @Test
    fun `sort by category changes orderby in state to category`() = runBlockingTest {
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Sort("category", false))
        val value = itemListViewModel.listUiState.getOrAwaitValue()
        assertThat(value.orderBy).isInstanceOf(OrderBy.Category::class.java)
    }

    @Test
    fun `sort by author changes orderby in state to author`() = runBlockingTest {
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Sort("author", false))
        val value = itemListViewModel.listUiState.getOrAwaitValue()
        assertThat(value.orderBy).isInstanceOf(OrderBy.Author::class.java)
    }

    @Test
    fun `sort by source changes orderby in state to source`() = runBlockingTest {
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Sort("source", false))
        val value = itemListViewModel.listUiState.getOrAwaitValue()
        assertThat(value.orderBy).isInstanceOf(OrderBy.Source::class.java)
    }

    @Test
    fun `sort by date changes orderby in state to date`() = runBlockingTest {
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Sort("date", false))
        val value = itemListViewModel.listUiState.getOrAwaitValue()
        assertThat(value.orderBy).isInstanceOf(OrderBy.Date::class.java)
    }

    @Test
    fun `sort by invalid changes orderby in state to date`() = runBlockingTest {
        itemListViewModel.performEvent(
            ItemListViewModel.ListUiEvent.Sort(
                "unknowncolumnAHVHA",
                false
            )
        )
        val value = itemListViewModel.listUiState.getOrAwaitValue()
        assertThat(value.orderBy).isInstanceOf(OrderBy.Date::class.java)
    }

    @Test
    fun `sort with same values multiple times performs query once`() = runBlockingTest {
        val newColumn = "author"
        val ascending = false
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Sort(newColumn, ascending))
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Sort(newColumn, ascending))
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Sort(newColumn, ascending))
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Sort(newColumn, ascending))
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Sort(newColumn, ascending))
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Sort(newColumn, ascending))
        // First call is when initializing, second call after the first change in sort
        verify(exactly = 2) { getNews(any(), any()) }
    }

    @Test
    fun `sort with new values performs query`() = runBlockingTest {
        val newColumn = "author"
        val ascending = false
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Sort(newColumn, ascending))
        verify(atLeast = 2) { getNews(any(), any()) }
    }

    @Test
    fun `refresh performs query`() = runBlockingTest {
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Refresh)
        verify(atLeast = 2) { getNews(any(), any()) }
    }

    @Test
    fun `create news calls create news use case with same values`() = runBlockingTest {
        val title = "Title"
        val newsAbstract = "Some abstract"
        val publishDate = Date()
        val category = "Category"
        val author = "Author"
        val source = "Source"
        val url = "url"
        val images = emptyList<NewsImage>()
        itemListViewModel.performEvent(
            ItemListViewModel.ListUiEvent.CreateNews
                (
                title,
                newsAbstract,
                publishDate,
                category,
                author,
                source,
                url,
                images
            )
        )
        coVerify(exactly = 1) {
            createNews(
                title,
                newsAbstract,
                publishDate,
                category,
                author,
                source,
                url,
                images
            )
        }
    }


    @Test
    fun `successful create news shows snackbar with success`() = runBlockingTest {
        coEvery {
            createNews(any(), any(), any(), any(), any(), any(), any(), any())
        } returns flow {
            emit(
                DataState.Success(
                    createRandomNews()
                )
            )
        }

        val title = "Title"
        val newsAbstract = "Some abstract"
        val publishDate = Date()
        val category = "Category"
        val author = "Author"
        val source = "Source"
        val url = "url"
        val images = emptyList<NewsImage>()
        itemListViewModel.performEvent(
            ItemListViewModel.ListUiEvent.CreateNews
                (
                title,
                newsAbstract,
                publishDate,
                category,
                author,
                source,
                url,
                images
            )
        )
        val singleEvent = itemListViewModel.singleEventState.getOrAwaitValue()
        assertThat(singleEvent).isInstanceOf(ItemListViewModel.ListSingleEvent.ShowSnackbar::class.java)
        assertThat((singleEvent as ItemListViewModel.ListSingleEvent.ShowSnackbar).message.lowercase())
            .containsMatch("success")
    }

    @Test
    fun `failed create news shows snackbar with failure`() = runBlockingTest {
        coEvery {
            createNews(any(), any(), any(), any(), any(), any(), any(), any())
        } returns flow {
            emit(
                DataState.Failure(
                    Exception("Unable to create news"), null
                )
            )
        }

        val title = "Title"
        val newsAbstract = "Some abstract"
        val publishDate = Date()
        val category = "Category"
        val author = "Author"
        val source = "Source"
        val url = "url"
        val images = emptyList<NewsImage>()
        itemListViewModel.performEvent(
            ItemListViewModel.ListUiEvent.CreateNews
                (
                title,
                newsAbstract,
                publishDate,
                category,
                author,
                source,
                url,
                images
            )
        )
        val singleEvent = itemListViewModel.singleEventState.getOrAwaitValue()
        assertThat(singleEvent).isInstanceOf(ItemListViewModel.ListSingleEvent.ShowSnackbar::class.java)
        assertThat((singleEvent as ItemListViewModel.ListSingleEvent.ShowSnackbar).message.lowercase())
            .containsMatch("error")
    }

    @Test
    fun `id on phone changes id of viewed news in ui state`() {
        val newsId = 1L
        val isTablet = false
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.ViewNews(newsId, isTablet))
        val viewedNews = itemListViewModel.listUiState.getOrAwaitValue().viewedNews
        assertThat(viewedNews).isEqualTo(newsId)
    }

    @Test
    fun `same id multiple times on phone changes id of viewed news in ui state`() {
        val newsId = 1L
        val isTablet = false
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.ViewNews(newsId, isTablet))
        val initialUi = itemListViewModel.listUiState.getOrAwaitValue()
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.ViewNews(newsId, isTablet))
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.ViewNews(newsId, isTablet))
        val finalUi = itemListViewModel.listUiState.getOrAwaitValue()
        assertThat(initialUi).isNotSameInstanceAs(finalUi)
    }

    @Test
    fun `different id of viewed news in tablet changes the id of viewed news`() {
        val newsId = 999L
        val isTablet = true
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.ViewNews(newsId, isTablet))
        val viewedNews = itemListViewModel.listUiState.getOrAwaitValue().viewedNews
        assertThat(viewedNews).isEqualTo(newsId)
    }

    @Test
    fun `same id multiple times on tablet does not change id of viewed news in ui state`() {
        val newsId = 999L
        val isTablet = true
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.ViewNews(newsId, isTablet))
        val initialUi = itemListViewModel.listUiState.getOrAwaitValue()
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.ViewNews(newsId, isTablet))
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.ViewNews(newsId, isTablet))
        val finalUi = itemListViewModel.listUiState.getOrAwaitValue()
        assertThat(initialUi).isSameInstanceAs(finalUi)
    }

    @Test
    fun `id on phone creates new ViewDetailEvent`() {
        val newsId = 1L
        val isTablet = false
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.ViewNews(newsId, isTablet))
        val singleEvent = itemListViewModel.singleEventState.getOrAwaitValue()
        assertThat(singleEvent).isInstanceOf(ItemListViewModel.ListSingleEvent.ViewDetail::class.java)
        assertThat((singleEvent as ItemListViewModel.ListSingleEvent.ViewDetail).id).isEqualTo(
            newsId
        )
    }

    @Test
    fun `same id on phone multiple times creates different ViewDetailEvent`() {
        val newsId = 1L
        val isTablet = false
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.ViewNews(newsId, isTablet))
        val initialEvent = itemListViewModel.singleEventState.getOrAwaitValue()
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.ViewNews(newsId, isTablet))
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.ViewNews(newsId, isTablet))
        val finalEvent = itemListViewModel.singleEventState.getOrAwaitValue()
        assertThat(initialEvent).isNotSameInstanceAs(finalEvent)
    }


    @Test
    fun `different id on tablet creates new ViewDetailEvent`() {
        val newsId = 999L
        val isTablet = true
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.ViewNews(newsId, isTablet))
        val singleEvent = itemListViewModel.singleEventState.getOrAwaitValue()
        assertThat(singleEvent).isInstanceOf(ItemListViewModel.ListSingleEvent.ViewDetail::class.java)
        assertThat((singleEvent as ItemListViewModel.ListSingleEvent.ViewDetail).id).isEqualTo(
            newsId
        )
    }

    @Test
    fun `same id on tablet multiple times does not create different ViewDetailEvent`() {
        val newsId = 1L
        val isTablet = true
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.ViewNews(newsId, isTablet))
        val initialEvent = itemListViewModel.singleEventState.getOrAwaitValue()
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.ViewNews(newsId, isTablet))
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.ViewNews(newsId, isTablet))
        val finalEvent = itemListViewModel.singleEventState.getOrAwaitValue()
        assertThat(initialEvent).isSameInstanceAs(finalEvent)
    }

    @Test
    fun `consume single event sets single event to null`() {
        itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.ConsumeSingleEvent)
        val singleEvent = itemListViewModel.singleEventState.getOrAwaitValue()
        assertThat(singleEvent).isNull()
    }

    @Test
    fun `perform query producing success changes UI State to success with data`() =
        runBlockingTest {
            every { getNews(any(), any()) } returns flow {
                emit(
                    DataState.Success(
                        flow {
                            emit(emptyList())
                        }
                    )
                )
            }

            itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Refresh)
            val (result, _, _, _) = itemListViewModel.listUiState.getOrAwaitValue()
            assertThat(result).isInstanceOf(DataState.Success::class.java)
            assertThat((result as DataState.Success).data).isNotNull()
        }

    @Test
    fun `perform query producing failure with data changes UI State to failure with data`() =
        runBlockingTest {
            every { getNews(any(), any()) } returns flow {
                emit(
                    DataState.Failure(
                        Exception("An error occurred"),
                        flow { emit(emptyList<News>()) })
                )
            }

            itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Refresh)
            val (result, _, _, _) = itemListViewModel.listUiState.getOrAwaitValue()
            assertThat(result).isInstanceOf(DataState.Failure::class.java)
            assertThat((result as DataState.Failure).data).isNotNull()
        }

    @Test
    fun `perform query producing failure without data changes UI State to failure without data`() =
        runBlockingTest {
            every {
                getNews(
                    any(),
                    any()
                )
            } returns flow { emit(DataState.Failure(Exception("An error occurred"))) }

            itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Refresh)
            val (result, _, _, _) = itemListViewModel.listUiState.getOrAwaitValue()
            assertThat(result).isInstanceOf(DataState.Failure::class.java)
            assertThat((result as DataState.Failure).data).isNull()
        }

    @Test
    fun `perform query producing loading with data changes UI State to loading with data`() =
        runBlockingTest {
            every { getNews(any(), any()) } returns flow {
                emit(
                    DataState.Loading(
                        flow { emit(emptyList()) })
                )
            }

            itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Refresh)
            val (result, _, _, _) = itemListViewModel.listUiState.getOrAwaitValue()
            assertThat(result).isInstanceOf(DataState.Loading::class.java)
            assertThat((result as DataState.Loading).data).isNotNull()
        }

    @Test
    fun `perform query producing loading without data changes UI State to loading without data`() =
        runBlockingTest {
            every { getNews(any(), any()) } returns flow { emit(DataState.Loading()) }

            itemListViewModel.performEvent(ItemListViewModel.ListUiEvent.Refresh)
            val (result, _, _, _) = itemListViewModel.listUiState.getOrAwaitValue()
            assertThat(result).isInstanceOf(DataState.Loading::class.java)
            assertThat((result as DataState.Loading).data).isNull()
        }

}
