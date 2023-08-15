package com.silent.ilustriscore.core.contract


enum class ErrorType(val message: String) {
    NOT_FOUND("Dados não encontrados"),
    UNKNOWN("Erro desconhecido"),
    UPDATE("Erro ao atualizar dados"),
    UPLOAD("Erro ao salvar arquivo"),
    DELETE("Erro ao remover dados"),
    SAVE("Erro ao salvar dados"),
    AUTH("Você precisa estar logado para continuar.")
}


sealed class DataError(val message: String) {
    data class Unknown(val exceptionMessage: String?) :
        DataError(exceptionMessage ?: ErrorType.UNKNOWN.message)

    object NotFound : DataError(ErrorType.NOT_FOUND.message)
    object Update : DataError(ErrorType.UPDATE.message)
    object Upload : DataError(ErrorType.UPLOAD.message)
    object Delete : DataError(ErrorType.DELETE.message)
    object Save : DataError(ErrorType.SAVE.message)
    object Auth : DataError(ErrorType.AUTH.message)
}

data class DataException(val code: ErrorType = ErrorType.UNKNOWN) {
    companion object {
        val NOTFOUND = DataException(ErrorType.NOT_FOUND)
        val UNKNOWN = DataException(ErrorType.UNKNOWN)
        val UPDATE = DataException(ErrorType.UPDATE)
        val UPLOAD = DataException(ErrorType.UPLOAD)
        val DELETE = DataException(ErrorType.DELETE)
        val SAVE = DataException(ErrorType.SAVE)
        val AUTH = DataException(ErrorType.AUTH)
    }
}