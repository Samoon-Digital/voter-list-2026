package com.samoondigital.yojnaplus.core.common

/**
 * A generic wrapper for the result of an operation that can succeed or fail.
 * Used between the data and domain layers to model network/database outcomes
 * without leaking exceptions across boundaries.
 */
sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val message: String, val throwable: Throwable? = null) : Resource<Nothing>
}

inline fun <T> safeCall(block: () -> T): Resource<T> = try {
    Resource.Success(block())
} catch (e: Exception) {
    Resource.Error(e.message ?: "Something went wrong", e)
}
