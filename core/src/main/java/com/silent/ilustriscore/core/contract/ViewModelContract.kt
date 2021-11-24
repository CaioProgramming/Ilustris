package com.silent.ilustriscore.core.contract

import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.model.BaseService

interface ViewModelContract<T> where T : BaseBean {
    val service: BaseService<T>
}