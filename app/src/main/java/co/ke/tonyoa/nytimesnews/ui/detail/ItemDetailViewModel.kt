package co.ke.tonyoa.nytimesnews.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.tonyoa.nytimesnews.domain.models.News
import co.ke.tonyoa.nytimesnews.domain.usecases.GetNewsById
import co.ke.tonyoa.nytimesnews.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(private val getNewsById: GetNewsById) : ViewModel() {

    private val _uiState = MutableLiveData<DataState<News?>>(DataState.Success(null))
    val uiState: LiveData<DataState<News?>>
        get() = _uiState

    private var lastId: Long? = null
    private var job: Job? = null

    fun performEvent(detailUiEvent: DetailUiEvent) {
        when (detailUiEvent) {
            is DetailUiEvent.GetNews -> getNews(detailUiEvent.id)
        }
    }

    private fun getNews(id: Long) {
        // Prevent fetching same data multiple times
        if (id == lastId)
            return
        lastId = id

        // Stop previous emissions of data
        job?.cancel()

        job = viewModelScope.launch {
            getNewsById(id)
                .collectLatest {
                    _uiState.value = it
                }
        }
    }

    sealed class DetailUiEvent {
        data class GetNews(val id: Long) : DetailUiEvent()
    }
}