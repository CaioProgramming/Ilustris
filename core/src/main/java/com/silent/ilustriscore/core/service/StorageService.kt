package com.silent.ilustriscore.core.service

import android.net.Uri
import com.silent.ilustriscore.core.contract.DataException
import com.silent.ilustriscore.core.contract.ServiceResult
import com.silent.ilustriscore.core.contract.StorageSettings
import kotlinx.coroutines.tasks.await
import java.io.File

class StorageService(override val dataPath: String) : StorageSettings {

    suspend fun uploadToStorage(uri: String): ServiceResult<DataException, String> {
        try {
            if (currentUser() == null) return ServiceResult.Error(DataException.AUTH)
            val file = File(uri)
            val uriFile = Uri.fromFile(file)
            val iconRef = storageReference().child(file.name)
            val uploadTask = iconRef.putFile(uriFile).await()
            return if (!uploadTask.task.isSuccessful) {
                ServiceResult.Error(DataException.UPLOAD)
            } else {
                val downloadUrl = uploadTask.storage.downloadUrl.await()
                if (downloadUrl == null) {
                    ServiceResult.Error(DataException.UPLOAD)
                } else {
                    ServiceResult.Success(downloadUrl.toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ServiceResult.Error(DataException.UPLOAD)
        }
    }


}