package com.silent.ilustriscore.core.model

import android.net.Uri
import com.silent.ilustriscore.core.bean.BaseBean

sealed class ViewModelBaseState {
    object RequireAuth : ViewModelBaseState()
    object DataDeletedState : ViewModelBaseState()
    object LoadingState : ViewModelBaseState()
    object LoadCompleteState : ViewModelBaseState()
    data class DataRetrievedState(val data: BaseBean) : ViewModelBaseState()
    data class DataListRetrievedState(val dataList: ArrayList<BaseBean>) : ViewModelBaseState()
    data class DataSavedState(val data: BaseBean) : ViewModelBaseState()
    data class DataUpdateState(val data: BaseBean) : ViewModelBaseState()
    data class FileUploadedState(val downloadUrl: Uri) : ViewModelBaseState()
    data class ErrorState(val dataException: DataException) : ViewModelBaseState()
}