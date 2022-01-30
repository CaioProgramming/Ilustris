package com.ilustris.app

import android.app.Application
import com.silent.ilustriscore.core.model.BaseViewModel

class IlustrisViewModel(application: Application) : BaseViewModel<AppDTO>(application) {

    lateinit var newAppDTO: AppDTO
    override val service = IlustrisService()

}