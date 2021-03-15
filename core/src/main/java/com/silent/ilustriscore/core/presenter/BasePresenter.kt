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
        try {
            view.onLoading()
            model.getSingleData(key)
        } catch (e: Exception) {
            view.error(DataException(e.message))
        } catch (data: DataException) {
            view.error(data)
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
        } catch (e: Exception) {
            view.error(DataException(e.message))
        } catch (d: DataException) {
            view.error(d)
        } finally {
            view.onLoadFinish()
        }
    }

    override fun updateData(data: T) {
        try {
            model.addData(data, data.id)
        } catch (e: Exception) {
            view.error(DataException.fromException(e))
        } catch (d: DataException) {
            view.error(d)
        } finally {
            view.onLoadFinish()
        }

    }

    override fun deleteData(data: T) {
        try {
            view.onLoading()
            model.deleteData(data.id)
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
            } catch (e: Exception) {
                view.error(DataException.fromException(e))
            } catch (d: DataException) {
                view.error(d)
            } finally {
                view.onLoadFinish()
            }
        }

    }

    override fun onSingleData(data: T) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                view.showData(data)
            } catch (e: Exception) {
                view.error(DataException.fromException(e))
            } catch (d: DataException) {
                view.error(d)
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
        } catch (e: Exception) {
            view.error(DataException.fromException(e))
        } catch (d: DataException) {
            view.error(d)
        } finally {
            view.onLoadFinish()
        }
    }

    fun findPreciseData(value: String, field: String) {
        view.onLoading()
        model.explicitSearch(value, field)
    }

}