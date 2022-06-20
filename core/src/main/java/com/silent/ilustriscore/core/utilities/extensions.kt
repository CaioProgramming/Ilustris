package com.silent.ilustriscore.core.utilities


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


