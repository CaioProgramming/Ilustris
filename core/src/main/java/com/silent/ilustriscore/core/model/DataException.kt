package com.silent.ilustriscore.core.model


enum class ErrorType(val message: String) {
    NOT_FOUND("Dados não encontrados"),
    UNKNOWN("Erro desconhecido"),
    UPDATE("Erro ao atualizar dados"),
    UPLOAD("Erro ao salvar arquivo"),
    DELETE("Erro ao remover dados"),
    SAVE("Erro ao salvar dados"),
}

data class DataException(
    val code: ErrorType = ErrorType.UNKNOWN
)