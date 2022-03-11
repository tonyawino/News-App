package co.ke.tonyoa.nytimesnews.ui.list

import androidx.lifecycle.*
import co.ke.tonyoa.nytimesnews.domain.models.News
import co.ke.tonyoa.nytimesnews.domain.models.NewsImage
import co.ke.tonyoa.nytimesnews.domain.models.OrderBy
import co.ke.tonyoa.nytimesnews.domain.models.OrderDirection
import co.ke.tonyoa.nytimesnews.domain.usecases.CreateNews
import co.ke.tonyoa.nytimesnews.domain.usecases.GetNews
import co.ke.tonyoa.nytimesnews.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@FlowPreview
@HiltViewModel
class ItemListViewModel @Inject constructor(
    private val getNews: GetNews,
    private val createNews: CreateNews
) : ViewModel() {

    private val _uiState = MutableLiveData(ListUiState())
    val listUiState: LiveData<ListUiState>
        get() = _uiState

    private val _singleEventState = MutableLiveData<ListSingleEvent?>()
    val singleEventState: LiveData<ListSingleEvent?>
        get() = _singleEventState

    private var job: Job? = null

    init {
        performQuery()
    }

    fun performEvent(listUiEvent: ListUiEvent) {
        val uiStateValue = _uiState.value ?: ListUiState()
        when (listUiEvent) {
            is ListUiEvent.Search -> {
                val newQuery = if (listUiEvent.query == null || listUiEvent.query.isBlank()) null
                else listUiEvent.query.trim()
                // Nothing has changed
                if (newQuery == uiStateValue.query)
                    return

                _uiState.value = uiStateValue.copy(query = newQuery)
                performQuery()
            }
            is ListUiEvent.Sort -> {
                val orderDirection = if (listUiEvent.ascending)
                    OrderDirection.ASCENDING
                else
                    OrderDirection.DESCENDING

                val sameTypeAsCurrent: Boolean
                val orderBy = when (listUiEvent.column.lowercase()) {
                    "title" -> {
                        sameTypeAsCurrent = uiStateValue.orderBy is OrderBy.Title
                        OrderBy.Title(orderDirection)
                    }
                    "category" -> {
                        sameTypeAsCurrent = uiStateValue.orderBy is OrderBy.Category
                        OrderBy.Category(orderDirection)
                    }
                    "author" -> {
                        sameTypeAsCurrent = uiStateValue.orderBy is OrderBy.Author
                        OrderBy.Author(orderDirection)
                    }
                    "source" -> {
                        sameTypeAsCurrent = uiStateValue.orderBy is OrderBy.Source
                        OrderBy.Source(orderDirection)
                    }
                    else -> {
                        sameTypeAsCurrent = uiStateValue.orderBy is OrderBy.Date
                        OrderBy.Date(orderDirection)
                    }
                }

                // Nothing has changed
                if (sameTypeAsCurrent && uiStateValue.orderBy.orderDirection == orderBy.orderDirection)
                    return

                _uiState.value = uiStateValue.copy(orderBy = orderBy)
                performQuery()
            }
            is ListUiEvent.Refresh -> {
                performQuery()
            }
            is ListUiEvent.CreateNews -> {
                createNewNews(listUiEvent)
            }
            is ListUiEvent.ViewNews -> viewSingleNews(listUiEvent.id, listUiEvent.isTablet)
            is ListUiEvent.ConsumeSingleEvent -> consumeSingleEvent()
        }
    }

    private fun viewSingleNews(id: Long, isTablet: Boolean) {
        val uiStateValue = _uiState.value
        // If is a tablet and item is being viewed already
        if (isTablet && uiStateValue?.viewedNews == id)
            return
        _uiState.value = uiStateValue?.copy(viewedNews = id)
        _singleEventState.value = ListSingleEvent.ViewDetail(id)
    }

    private fun consumeSingleEvent() {
        _singleEventState.value = null
    }

    private fun createNewNews(listUiEvent: ListUiEvent.CreateNews) {
        viewModelScope.launch {
            createNews(
                listUiEvent.title,
                listUiEvent.newsAbstract,
                listUiEvent.publishDate,
                listUiEvent.category,
                listUiEvent.author,
                listUiEvent.source,
                listUiEvent.url,
                listUiEvent.images
            ).collectLatest {
                when (it) {
                    is DataState.Success -> {
                        _singleEventState.value =
                            ListSingleEvent.ShowSnackbar("Successfully saved ${it.data?.title}")
                    }
                    is DataState.Failure -> {
                        _singleEventState.value =
                            ListSingleEvent.ShowSnackbar("An error occurred while saving ${it.data?.title}")
                    }
                    is DataState.Loading -> {

                    }
                }
            }
        }
    }

    private fun performQuery() {
        // Stop previous emissions of data
        job?.cancel()

        val uiStateValue = _uiState.value ?: ListUiState()
        job = viewModelScope.launch {
            getNews(uiStateValue.query, uiStateValue.orderBy)
                // Wait half a second in case user is still typing
                .debounce(500)
                .collectLatest {
                    when (it) {
                        // Watch for database changes of the results
                        is DataState.Success -> {
                            it.data.collectLatest { list ->
                                _uiState.value = uiStateValue.copy(result = DataState.Success(list))
                            }
                        }
                        // Watch for database changes of the results if they are available
                        is DataState.Failure -> {
                            if (it.data != null) {
                                it.data.collectLatest { list ->
                                    _uiState.value = uiStateValue.copy(
                                        result = DataState.Failure(
                                            it.throwable,
                                            list
                                        )
                                    )
                                }
                            } else {
                                _uiState.value = uiStateValue.copy(
                                    result = DataState.Failure(
                                        it.throwable,
                                        null
                                    )
                                )
                            }
                        }
                        // Watch for database changes of results if they are available
                        is DataState.Loading -> {
                            if (it.data != null) {
                                it.data.collectLatest { list ->
                                    _uiState.value =
                                        uiStateValue.copy(result = DataState.Loading(list))
                                }
                            } else {
                                _uiState.value = uiStateValue.copy(result = DataState.Loading())
                            }
                        }
                    }
                }
        }
    }

    sealed class ListUiEvent {
        data class Search(val query: String?) : ListUiEvent()
        data class Sort(val column: String, val ascending: Boolean) : ListUiEvent()
        data class CreateNews(
            val title: String,
            val newsAbstract: String,
            val publishDate: Date,
            val category: String,
            val author: String,
            val source: String,
            val url: String,
            val images: List<NewsImage>
        ) : ListUiEvent()

        data class ViewNews(val id: Long, val isTablet: Boolean) : ListUiEvent()
        object Refresh : ListUiEvent()
        object ConsumeSingleEvent : ListUiEvent()
    }

    sealed class ListSingleEvent {
        data class ShowSnackbar(val message: String) : ListSingleEvent()
        data class ViewDetail(val id: Long) : ListSingleEvent()
    }

    data class ListUiState(
        val result: DataState<List<News>> = DataState.Loading(),
        val query: String? = null,
        val orderBy: OrderBy = OrderBy.Date(OrderDirection.DESCENDING),
        val viewedNews: Long? = null
    )
}