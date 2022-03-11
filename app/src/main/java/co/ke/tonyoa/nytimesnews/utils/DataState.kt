package co.ke.tonyoa.nytimesnews.utils

sealed class DataState<out R> {
    data class Success<out T>(val data: T) : DataState<T>()
    data class Failure<out T>(val throwable: Throwable, val data: T? = null) : DataState<T>()
    data class Loading<out T>(val data: T? = null) : DataState<T>()
}