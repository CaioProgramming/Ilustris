package com.silent.ilustriscore.core.contract

import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.model.BaseModel
import com.silent.ilustriscore.core.model.DTOMessage
import com.silent.ilustriscore.core.model.DataException
import com.silent.ilustriscore.core.view.BaseView

interface PresenterContract<T> where T : BaseBean {

    val view: BaseView<T>
    val model: BaseModel<T>

    fun saveData(data: T, forcedID: String? = null)
    fun deleteData(data: T)
    fun updateData(data: T)
    fun loadSingleData(key: String)
    fun queryData(value: String, field: String)
    fun onDataRetrieve(data: List<T>)
    fun onSingleData(data: T)
    fun modelCallBack(dtoMessage: DTOMessage)
    fun errorCallBack(dataException: DataException)
}