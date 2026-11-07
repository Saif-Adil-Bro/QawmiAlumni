package com.qawmialumni.app.core.util

/**
 * Generic wrapper for UI-facing operation results.
 */
sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>
}

inline fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> = when (this) {
    is Resource.Success -> Resource.Success(transform(data))
    is Resource.Error -> this
    Resource.Loading -> Resource.Loading
}
