package com.silent.ilustriscore.core.model


enum class ErrorType { NOT_FOUND, UNKNOWN, DISCONNECTED, UPDATE, DELETE }
class DataException(message: String? = "Unknown error", code: ErrorType = ErrorType.UNKNOWN) : Exception() {

    companion object {
        fun fromException(e: Exception): DataException {
            return DataException(e.message)
        }
    }

}