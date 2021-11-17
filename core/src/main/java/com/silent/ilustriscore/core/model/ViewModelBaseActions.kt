package com.silent.ilustriscore.core.model

import com.silent.ilustriscore.core.bean.BaseBean

sealed class ViewModelBaseActions {

    object GetAllDataAction : ViewModelBaseActions()
    data class GetDataByIdAction(val id: String) : ViewModelBaseActions()
    data class UpdateDataAction(val data: BaseBean) : ViewModelBaseActions()
    data class DeleteDataAction(val id: String) : ViewModelBaseActions()
    data class UploadFileAction(val uri: String) : ViewModelBaseActions()
    data class SaveDataAction(val data: BaseBean) : ViewModelBaseActions()
    data class SavePreciseDataAction(val data: BaseBean, val forcedID: String) :
        ViewModelBaseActions()

    data class QueryDataAction(val field: String, val value: String) : ViewModelBaseActions()
    data class PreciseQueryDataAction(val field: String, val value: String) : ViewModelBaseActions()

}