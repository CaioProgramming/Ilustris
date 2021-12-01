package com.silent.ilustriscore.core.model

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.contract.ViewModelContract
import kotlinx.coroutines.launch

abstract class BaseViewModel<T> : ViewModel(), ViewModelContract<T> where T : BaseBean {

    val viewModelState = MutableLiveData<ViewModelBaseState>()

    fun isAuthenticated(): Boolean = service.currentUser != null

    private fun updateViewState(viewModelBaseState: ViewModelBaseState) {
        viewModelState.postValue(viewModelBaseState)
    }

    private fun sendErrorState(dataException: DataException) {
        Log.e(javaClass.simpleName, "sendErrorState: $dataException")
        updateViewState(ViewModelBaseState.ErrorState(dataException))
    }

    fun dispatchViewAction(viewAction: ViewModelBaseActions) {
        when (viewAction) {
            is ViewModelBaseActions.DeleteDataAction -> deleteData(viewAction.id)
            ViewModelBaseActions.GetAllDataAction -> getAllData()
            is ViewModelBaseActions.GetDataByIdAction -> getSingleData(viewAction.id)
            is ViewModelBaseActions.PreciseQueryDataAction -> explicitSearch(
                viewAction.value,
                viewAction.field
            )
            is ViewModelBaseActions.QueryDataAction -> query(viewAction.value, viewAction.field)
            is ViewModelBaseActions.SaveDataAction -> saveData(viewAction.data as T)
            is ViewModelBaseActions.UpdateDataAction -> editData(viewAction.data as T)
            is ViewModelBaseActions.UploadFileAction -> uploadFile(viewAction.uri)
            is ViewModelBaseActions.SavePreciseDataAction -> editData(viewAction.data as T)
        }
    }

    fun checkAuth() {
        if (isAuthenticated()) updateViewState(ViewModelBaseState.RequireAuth)
    }

   open fun uploadFile(uri: String) {
       viewModelScope.launch {
           val result = service.uploadToStorage(uri)
           if (result.isSuccess) {
               updateViewState(ViewModelBaseState.FileUploadedState(Uri.parse(result.success.data)))
           } else sendErrorState(result.error.errorException)
       }
   }

    open fun editData(data: T) {
        viewModelScope.launch {
            val result = service.editData(data)
            if (result.isSuccess) {
                updateViewState(ViewModelBaseState.DataUpdateState(result.success.data))
            }
        }
    }

    open fun saveData(data: T) {
        viewModelScope.launch {
            val result = service.addData(data)
            if (result.isSuccess) {
                updateViewState(ViewModelBaseState.DataSavedState(data))
            } else sendErrorState(result.error.errorException)
        }
    }

    open fun getSingleData(id: String) {
        viewModelScope.launch {
            val result = service.getSingleData(id)
            if (result.isSuccess) {
                updateViewState(ViewModelBaseState.DataRetrievedState(result.success.data))
            } else sendErrorState(result.error.errorException)
        }
    }

    open fun explicitSearch(value: String, field: String) {
        viewModelScope.launch {
            val result = service.explicitSearch(value, field)
            if (result.isSuccess) {
                updateViewState(ViewModelBaseState.DataListRetrievedState(result.success.data))
            } else sendErrorState(result.error.errorException)
        }
    }

    open fun query(value: String, field: String) {
        viewModelScope.launch {
            val result = service.query(value, field)
            if (result.isSuccess) {
                updateViewState(ViewModelBaseState.DataListRetrievedState(result.success.data))
            } else sendErrorState(result.error.errorException)
        }
    }

    open fun deleteData(id: String) {
        viewModelScope.launch {
            val result = service.deleteData(id)
            if (result.isSuccess) {
                updateViewState(ViewModelBaseState.DataDeletedState)
            } else sendErrorState(result.error.errorException)
        }
    }

    fun getAllData() {
        viewModelScope.launch {
            val result = service.getAllData()
            if (result.isSuccess) {
                updateViewState(ViewModelBaseState.DataListRetrievedState(result.success.data))
            } else sendErrorState(result.error.errorException)
        }
    }
}
