package com.ilustris.app

import com.silent.ilustriscore.core.model.BaseModel
import com.silent.ilustriscore.core.presenter.BasePresenter
import com.silent.ilustriscore.core.view.BaseView

class IlustrisPresenter(override val view: BaseView<AppDTO>) : BasePresenter<AppDTO>() {
    override val model = IlustrisModel(this)

}