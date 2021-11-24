package com.silent.ilustriscore.core.model

sealed class ServiceResult<out L, out R> {

    data class Error<out L>(val errorException: DataException) : ServiceResult<L, Nothing>()

    data class Success<out R>(val data: R) : ServiceResult<Nothing, R>()

    val isSuccess get() = this is Success<R>

    val isError get() = this is Error<L>

    val success get() = this as Success
    val error get() = this as Error

}
