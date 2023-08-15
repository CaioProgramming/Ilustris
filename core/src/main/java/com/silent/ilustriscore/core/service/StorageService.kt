package com.silent.ilustriscore.core.service

import android.net.Uri
import com.silent.ilustriscore.core.contract.DataError
import com.silent.ilustriscore.core.contract.ServiceResult
import com.silent.ilustriscore.core.contract.StorageSettings
import kotlinx.coroutines.tasks.await

abstract class StorageService(override val dataPath: String) : StorageSettings {

    override val requireAuth: Boolean = true
    private fun authError(): ServiceResult.Error<DataError> {
        return ServiceResult.Error(DataError.Auth)
    }


    suspend fun uploadToStorage(
        uri: String,
        fileName: String
    ): ServiceResult<DataError, String> {
        try {
            if (currentUser() == null) return authError()
            val file = Uri.parse(uri)
            val uploadTask = storageReference().child(fileName).putFile(file).await()
            return if (!uploadTask.task.isSuccessful) {
                ServiceResult.Error(DataError.Upload)
            } else {
                val downloadUrl = uploadTask.storage.downloadUrl.await()
                if (downloadUrl == null) {
                    ServiceResult.Error(DataError.Upload)
                } else {
                    ServiceResult.Success(downloadUrl.toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ServiceResult.Error(DataError.Unknown(e.message))
        }
    }


}