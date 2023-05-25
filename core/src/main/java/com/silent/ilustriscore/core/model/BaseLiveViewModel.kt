package com.silent.ilustriscore.core.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.silent.ilustriscore.BuildConfig
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.contract.DataException
import com.silent.ilustriscore.core.contract.LiveViewModelContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseLiveViewModel<T>(application: Application) : AndroidViewModel(application),
    LiveViewModelContract where T : BaseBean {

    fun getUser() = liveService.currentUser()

    val viewModelState = MutableLiveData<ViewModelBaseState>()

    fun isAuthenticated(): Boolean = liveService.currentUser() != null

    fun updateViewState(viewModelBaseState: ViewModelBaseState) {
        viewModelState.postValue(viewModelBaseState)
    }

    protected fun sendErrorState(dataException: DataException) {
        if (BuildConfig.DEBUG) {
            Log.e(javaClass.simpleName, "sendErrorState: $dataException")
        }
        updateViewState(ViewModelBaseState.ErrorState(dataException))
    }

    fun checkAuth() {
        if (isAuthenticated()) updateViewState(ViewModelBaseState.RequireAuth)
    }

    open fun editData(data: T) {
        viewModelScope.launch(Dispatchers.IO) {
            updateViewState(ViewModelBaseState.LoadingState)
            val result = liveService.editData(data)
            if (result.isSuccess) {
                updateViewState(ViewModelBaseState.DataUpdateState(result.success.data))
            } else {
                updateViewState(ViewModelBaseState.ErrorState(DataException.UPDATE))
            }
        }
    }

    open fun saveData(data: T) {
        viewModelScope.launch(Dispatchers.IO) {
            updateViewState(ViewModelBaseState.LoadingState)
            val result = liveService.addData(data)
            if (result.isSuccess) {
                updateViewState(ViewModelBaseState.DataSavedState(data))
            } else sendErrorState(result.error.errorException)
        }
    }

    open fun getSingleData(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateViewState(ViewModelBaseState.LoadingState)
            liveService.getSingleData(id).collect { result ->
                if (result.isSuccess) {
                    updateViewState(ViewModelBaseState.DataRetrievedState(result.success.data))
                } else {
                    sendErrorState(result.error.errorException)
                }
            }
        }
    }

    open fun query(value: String, field: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateViewState(ViewModelBaseState.LoadingState)
            liveService.query(value, field).collect { result ->
                if (result.isSuccess) {
                    updateViewState(ViewModelBaseState.DataListRetrievedState(result.success.data))
                } else {
                    sendErrorState(result.error.errorException)
                }
            }
        }
    }

    open fun deleteData(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateViewState(ViewModelBaseState.LoadingState)
            val result = liveService.deleteData(id)
            if (result.isSuccess) {
                updateViewState(ViewModelBaseState.DataDeletedState)
            } else sendErrorState(result.error.errorException)
        }
    }

    open fun getAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            updateViewState(ViewModelBaseState.LoadingState)
            liveService.getAllData().collect { result ->
                if (result.isSuccess) {
                    updateViewState(ViewModelBaseState.DataListRetrievedState(result.success.data))
                } else {
                    sendErrorState(result.error.errorException)
                }
            }
        }
    }


}
