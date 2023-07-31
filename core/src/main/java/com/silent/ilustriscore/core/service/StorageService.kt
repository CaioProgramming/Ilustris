package com.silent.ilustriscore.core.service

import android.net.Uri
import com.silent.ilustriscore.core.contract.DataException
import com.silent.ilustriscore.core.contract.ServiceResult
import com.silent.ilustriscore.core.contract.StorageSettings
import kotlinx.coroutines.tasks.await

abstract class StorageService(override val dataPath: String) : StorageSettings {

    override val requireAuth: Boolean = true

    suspend fun uploadToStorage(
        uri: String,
        fileName: String? = null
    ): ServiceResult<DataException, String> {
        try {
            if (currentUser() == null) return ServiceResult.Error(DataException.AUTH)
            val file = Uri.parse(uri)
            val iconRef =
                storageReference().child(fileName ?: "IlustrisApp${System.currentTimeMillis()}")
            val uploadTask = iconRef.putFile(file).await()
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