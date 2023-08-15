package com.silent.ilustriscore.core.utilities


import com.silent.ilustriscore.core.contract.DataError
import com.silent.ilustriscore.core.contract.ServiceResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun delayedFunction(delayTime: Long = 1000, function: () -> Unit) {
    GlobalScope.launch(Dispatchers.IO) {
        delay(delayTime)
        GlobalScope.launch(Dispatchers.Main) {
            function.invoke()
        }

    }
}

internal fun authError(): ServiceResult.Error<DataError> {
    return ServiceResult.Error(DataError.Auth)
}


