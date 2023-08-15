package com.silent.ilustriscore.core.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.silent.ilustriscore.BuildConfig
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.contract.DataError
import com.silent.ilustriscore.core.contract.ViewModelContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseViewModel<T>(application: Application) : AndroidViewModel(application),
    ViewModelContract where T : BaseBean {

    fun getUser() = service.getCurrentUser()

    val viewModelState = MutableLiveData<ViewModelBaseState>()

    fun isAuthenticated(): Boolean = getUser() != null

    fun updateViewState(viewModelBaseState: ViewModelBaseState) {
        viewModelState.postValue(viewModelBaseState)
    }

    protected fun sendErrorState(dataException: DataError) {
        if (BuildConfig.DEBUG) {
            Log.e(javaClass.simpleName, "sendErrorState: $dataException")
        }
        updateViewState(ViewModelBaseState.ErrorState(dataException))
    }

    fun checkAuth() {
        if (isAuthenticated()) sendErrorState(DataError.Auth)
    }

    open fun editData(data: T) {
        viewModelScope.launch(Dispatchers.IO) {
            updateViewState(ViewModelBaseState.LoadingState)
            val result = service.editData(data)
            if (result.isSuccess) {
                updateViewState(ViewModelBaseState.DataUpdateState(result.success.data))
            } else {
                sendErrorState(result.error.errorException)
            }
        }
    }

    open fun saveData(data: T) {
        viewModelScope.launch(Dispatchers.IO) {
            updateViewState(ViewModelBaseState.LoadingState)
            val result = service.addData(data)
            if (result.isSuccess) {
                updateViewState(ViewModelBaseState.DataSavedState(data))
            } else sendErrorState(result.error.errorException)
        }
    }

    open fun getSingleData(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateViewState(ViewModelBaseState.LoadingState)
            val result = service.getSingleData(id)
            if (result.isSuccess) {
                updateViewState(ViewModelBaseState.DataRetrievedState(result.success.data))
            } else sendErrorState(result.error.errorException)
        }
    }

    open fun explicitSearch(value: String, field: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateViewState(ViewModelBaseState.LoadingState)
            val result = service.explicitSearch(value, field)
            if (result.isSuccess) {
                updateViewState(ViewModelBaseState.DataListRetrievedState(result.success.data))
            } else sendErrorState(result.error.errorException)
        }
    }

    open fun query(value: String, field: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateViewState(ViewModelBaseState.LoadingState)
            val result = service.query(value, field)
            if (result.isSuccess) {
                updateViewState(ViewModelBaseState.DataListRetrievedState(result.success.data))
            } else sendErrorState(result.error.errorException)
        }
    }

    open fun deleteData(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateViewState(ViewModelBaseState.LoadingState)
            val result = service.deleteData(id)
            if (result.isSuccess) {
                updateViewState(ViewModelBaseState.DataDeletedState)
            } else sendErrorState(result.error.errorException)
        }
    }

    open fun getAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            updateViewState(ViewModelBaseState.LoadingState)
            val result = service.getAllData()
            if (result.isSuccess) {
                updateViewState(ViewModelBaseState.DataListRetrievedState(result.success.data))
            } else sendErrorState(result.error.errorException)
        }
    }


}
