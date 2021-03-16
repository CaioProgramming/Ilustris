package com.silent.ilustriscore.core.model


enum class ErrorType { NOT_FOUND, UNKNOWN, DISCONNECTED, UPDATE, DELETE, SAVE }
data class DataException(val message: String? = "Unknown error", val code: ErrorType = ErrorType.UNKNOWN) {

    companion object {
        fun fromException(e: Exception): DataException {
            return DataException(e.message)
        }
    }

}