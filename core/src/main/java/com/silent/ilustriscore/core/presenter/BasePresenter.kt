package com.silent.ilustriscore.core.presenter

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.contract.PresenterContract
import com.silent.ilustriscore.core.model.DTOMessage
import com.silent.ilustriscore.core.model.DataException
import com.silent.ilustriscore.core.utilities.MessageType
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


abstract class BasePresenter<T> : PresenterContract<T> where T : BaseBean {

    val user by lazy {
        model.currentUser
    }

    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        Log.e(javaClass.simpleName, "an error ocurred: $throwable ")
        view.error(DataException.fromThrowable(throwable))
    }

    fun loadData() {
        view.onLoading()
        model.getAllData()
    }

    override fun loadSingleData(key: String) {
        try {
            view.onLoading()
            model.getSingleData(key)
        } catch (data: DataException) {
            view.error(data)
        } catch (e: Exception) {
            view.error(DataException(e.message))
        } finally {
            view.onLoadFinish()
        }
    }

    override fun saveData(data: T, forcedID: String?) {
        try {
            view.onLoading()
            if (forcedID.isNullOrBlank()) {
                model.addData(data)
            } else {
                model.addData(data, forcedID)
            }
        } catch (d: DataException) {
            view.error(d)
        } catch (e: Exception) {
            view.error(DataException(e.message))
        } finally {
            view.onLoadFinish()
        }
    }

    override fun updateData(data: T) {
        try {
            model.addData(data, data.id)
        } catch (d: DataException) {
            view.error(d)
        } catch (e: Exception) {
            view.error(DataException.fromException(e))
        } finally {
            view.onLoadFinish()
        }

    }

    override fun deleteData(data: T) {
        try {
            view.onLoading()
            model.deleteData(data.id)
        } catch (d: DataException) {
            view.error(d)
        } catch (e: Exception) {
            view.error(DataException.fromException(e))
        } finally {
            view.onLoadFinish()
        }
    }

    override fun onDataRetrieve(data: List<T>) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                view.showListData(data)
            } catch (d: DataException) {
                view.error(d)
            } catch (e: Exception) {
                view.error(DataException.fromException(e))
            } finally {
                view.onLoadFinish()
            }
        }

    }

    override fun onSingleData(data: T) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                view.showData(data)
            } catch (d: DataException) {
                view.error(d)
            } catch (e: Exception) {
                view.error(DataException.fromException(e))
            } finally {
                view.onLoadFinish()
            }
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

    override fun queryData(value: String, field: String) {
        try {
            view.onLoading()
            model.query(value, field)
        } catch (d: DataException) {
            view.error(d)
        } catch (e: Exception) {
            view.error(DataException.fromException(e))
        } finally {
            view.onLoadFinish()
        }
    }

    fun findPreciseData(value: String, field: String) {
        view.onLoading()
        model.explicitSearch(value, field)
    }

}