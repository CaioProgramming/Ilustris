package com.silent.ilustriscore.core.contract

import com.silent.ilustriscore.core.service.BaseLiveService
import com.silent.ilustriscore.core.service.BaseService

interface ViewModelContract {
    val service: BaseService
}

interface LiveViewModelContract {
    val liveService: BaseLiveService
}