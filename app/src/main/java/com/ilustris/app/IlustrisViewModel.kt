package com.ilustris.app

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.silent.ilustriscore.core.model.BaseLiveViewModel
import com.silent.ilustriscore.core.model.ViewModelBaseState
import com.silent.ilustriscore.core.service.StorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class IlustrisViewModel(application: Application) : BaseLiveViewModel<AppDTO>(application) {

    private val storageService by lazy {
        StorageService("Apps")
    }

    fun saveApp(app: AppDTO) {
        updateViewState(ViewModelBaseState.LoadingState)
        viewModelScope.launch(Dispatchers.IO) {
            val fileUploadTask = storageService.uploadToStorage(app.icon, app.name)
            if (fileUploadTask.isSuccess) {
                app.icon = fileUploadTask.success.data
                saveData(app)
            } else {
                sendErrorState(fileUploadTask.error.errorException)
            }
        }
    }

    lateinit var newAppDTO: AppDTO
    override val liveService = IlustrisLiveService()

}