package com.silent.ilustriscore.core.presenter

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.contract.PresenterContract
import com.silent.ilustriscore.core.model.DTOMessage
import com.silent.ilustriscore.core.model.DataException
import com.silent.ilustriscore.core.utilities.MessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


abstract class BasePresenter<T> : PresenterContract<T> where T : BaseBean {

    val user by lazy {
        model.currentUser
    }

    fun loadData() {
        view.onLoading()
        model.getAllData()
    }

    override fun loadSingleData(key: String) {
        view.onLoading()
        model.getSingleData(key)

    }

    override fun saveData(data: T, forcedID: String?) {
        view.onLoading()
        if (forcedID.isNullOrBlank()) {
            model.addData(data)
        } else {
            model.addData(data, forcedID)
        }
        view.onLoadFinish()
    }

    override fun updateData(data: T) {
        model.addData(data, data.id)
    }

    override fun deleteData(data: T) {
        model.deleteData(data.id)
    }

    override fun onDataRetrieve(data: List<T>) {
        GlobalScope.launch(Dispatchers.Main) {
            view.showListData(data)
            view.onLoadFinish()
        }

    }

    override fun onSingleData(data: T) {
        GlobalScope.launch(Dispatchers.Main) {
            view.showData(data)
            view.onLoadFinish()
        }

    }

    override fun modelCallBack(dtoMessage: DTOMessage) {
        GlobalScope.launch(Dispatchers.Main) {
            val priority = when (dtoMessage.type) {
                MessageType.ERROR -> Log.ERROR
                MessageType.SUCCESS -> Log.DEBUG
                MessageType.WARNING -> Log.WARN
                MessageType.INFO -> Log.INFO
            }
            Log.println(priority, javaClass.simpleName, dtoMessage.message)
            view.getCallBack(dtoMessage)
        }
    }

    override fun errorCallBack(dataException: DataException) {
        Log.i(javaClass.simpleName, "errorCallBack: $dataException")
        view.error(dataException)
    }

    override fun queryData(value: String, field: String) {
        view.onLoading()
        model.query(value, field)
    }

    fun findPreciseData(value: String, field: String) {
        view.onLoading()
        model.explicitSearch(value, field)
    }

}