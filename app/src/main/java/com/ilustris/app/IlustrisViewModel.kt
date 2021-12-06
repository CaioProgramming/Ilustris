package com.ilustris.app

import com.silent.ilustriscore.core.model.BaseService
import com.silent.ilustriscore.core.model.BaseViewModel

class IlustrisViewModel : BaseViewModel<AppDTO>() {

    lateinit var newAppDTO: AppDTO
    override val service = IlustrisService()

}