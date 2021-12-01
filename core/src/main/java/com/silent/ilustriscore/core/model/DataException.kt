package com.silent.ilustriscore.core.model


enum class ErrorType(val message: String) {
    NOT_FOUND("Dados não encontrados"),
    UNKNOWN("Erro desconhecido"),
    UPDATE("Erro ao atualizar dados"),
    UPLOAD("Erro ao salvar arquivo"),
    DELETE("Erro ao remover dados"),
    SAVE("Erro ao salvar dados"),
    AUTH("É preciso estar logado para realizar essa operação")
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