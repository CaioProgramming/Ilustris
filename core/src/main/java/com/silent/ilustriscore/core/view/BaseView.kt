package com.silent.ilustriscore.core.view

import android.content.Context
import android.util.Log
import com.silent.ilustriscore.core.contract.ViewContract
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.model.DTOMessage
import com.silent.ilustriscore.core.model.DataException
import com.silent.ilustriscore.core.utilities.MessageType

abstract class BaseView<T> : ViewContract<T> where  T : BaseBean {


    override fun getCallBack(dtoMessage: DTOMessage) {
        val priority = when (dtoMessage.type) {
            MessageType.ERROR -> Log.ERROR
            MessageType.SUCCESS -> Log.DEBUG
            MessageType.WARNING -> Log.WARN
            MessageType.INFO -> Log.INFO
        }
        Log.println(priority, javaClass.simpleName, dtoMessage.message)
    }

    override val context: Context by lazy { viewBind.root.context }

    override fun onLoading() {
        Log.i(javaClass.simpleName, "onLoading called")
    }

    override fun onLoadFinish() {
        Log.i(javaClass.simpleName, "onLoadFinish called")
    }

    override fun showListData(list: List<T>) {
        Log.i(javaClass.simpleName, "showListData(${list.size}): $list")
    }

    override fun showData(data: T) {
        Log.i(javaClass.simpleName, "showData: $data")
    }

    override fun success(message: String) {
        Log.d(javaClass.simpleName, "success: $message")
    }

    override fun error(dataException: DataException) {
        Log.e(javaClass.simpleName, "error: $dataException")
    }

}