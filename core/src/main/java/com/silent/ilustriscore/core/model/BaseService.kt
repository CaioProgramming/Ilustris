package com.silent.ilustriscore.core.model

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.contract.ServiceContract
import com.silent.ilustriscore.core.utilities.SEARCH_SUFFIX
import kotlinx.coroutines.tasks.await
import java.io.File

abstract class BaseService<T> : ServiceContract<T> where T : BaseBean {

    var requireAuth: Boolean = true

    val reference: CollectionReference by lazy {
        FirebaseFirestore.getInstance().collection(dataPath)
    }

    val currentUser: FirebaseUser? by lazy {
        FirebaseAuth.getInstance().currentUser
    }

    override suspend fun deleteData(id: String): ServiceResult<DataException, Boolean> {
        return try {
            reference.document(id).delete().await()
            ServiceResult.Success(true)
        } catch (e: Exception) {
            ServiceResult.Error(
                DataException(ErrorType.DELETE)
            )
        }
    }

    private fun errorMessage(
        errorType: ErrorType = ErrorType.UNKNOWN
    ): DataException = DataException(errorType)

    override suspend fun query(
        query: String,
        field: String
    ): ServiceResult<DataException, ArrayList<T>> {
        Log.i(javaClass.simpleName, "query: Buscando por $query em $field na collection $dataPath")
        val query = reference.orderBy(field).startAt(query).endAt(query + SEARCH_SUFFIX).get()
            .await().documents
        return if (query.isNotEmpty()) {
            ServiceResult.Success(getDataList(query))
        } else {
            ServiceResult.Error(
                errorMessage(
                    ErrorType.NOT_FOUND
                )
            )
        }
    }

    private fun getDataList(querySnapshot: MutableList<DocumentSnapshot>): ArrayList<T> {
        return ArrayList<T>().apply {
            querySnapshot.forEach {
                add(deserializeDataSnapshot(it))
            }
        }
    }

    suspend fun explicitSearch(
        query: String,
        field: String
    ): ServiceResult<DataException, ArrayList<T>> {
        Log.i(javaClass.simpleName, "query: Buscando por $query em $field na collection $dataPath")
        val query = reference.whereEqualTo(field, query).get().await().documents
        return if (query.isNotEmpty()) {
            ServiceResult.Success(getDataList(query))
        } else {
            ServiceResult.Error(errorMessage(ErrorType.NOT_FOUND))
        }
    }

    override suspend fun getAllData(): ServiceResult<DataException, ArrayList<T>> {
        val data = reference.get().await().documents
        return if (data.isNotEmpty()) {
            Log.i(javaClass.simpleName, "getAllData: $data")
            ServiceResult.Success(getDataList(data))
        } else ServiceResult.Error(
            errorMessage()
        )
    }

    override suspend fun getSingleData(id: String): ServiceResult<DataException, T> {
        val document = reference.document(id).get().await()
        return if (document != null) {
            Log.i(javaClass.simpleName, "getSingleData: $document")
            ServiceResult.Success(deserializeDataSnapshot(document))
        } else {
            ServiceResult.Error(errorMessage(errorType = ErrorType.NOT_FOUND))
        }
    }

    override suspend fun editData(data: T): ServiceResult<DataException, T> {
        return try {
            val task = reference.document(data.id).set(data).await()
            if (task != null) {
                Log.i(javaClass.simpleName, "edited -> $data")
                ServiceResult.Success(data)
            } else {
                ServiceResult.Error(errorMessage(errorType = ErrorType.UPDATE))
            }
        } catch (e: Exception) {
            ServiceResult.Error(errorMessage(errorType = ErrorType.UPDATE))

        }
    }

    suspend fun editField(
        data: Any,
        id: String,
        field: String
    ): ServiceResult<DataException, String> {
        return try {
            val task = reference.document(id).update(field, data).await()
            if (task != null) {
                Log.i(javaClass.simpleName, "edit successful: $data")
                ServiceResult.Success("Dados atualizados")
            } else ServiceResult.Error(errorMessage(ErrorType.UPDATE))
        } catch (e: Exception) {
            ServiceResult.Error(errorMessage(ErrorType.UPDATE))
        }
    }


    override suspend fun addData(data: T): ServiceResult<DataException, T> {
        return try {
            val task = reference.add(data).await()
            if (task != null) {
                Log.d(javaClass.simpleName, "data saved -> $data")
                ServiceResult.Success(data)
            } else ServiceResult.Error(
                errorMessage(ErrorType.SAVE)
            )
        } catch (e: Exception) {
            ServiceResult.Error(
                errorMessage(ErrorType.SAVE)
            )
        }
    }


    suspend fun uploadToStorage(uri: String): ServiceResult<DataException, String> {
        try {
            val file = File(uri)
            val uriFile = Uri.fromFile(file)
            val iconRef = FirebaseStorage.getInstance().reference.child("$dataPath/${file.name}")
            val uploadTask = iconRef.putFile(uriFile).await()
            return if (!uploadTask.task.isSuccessful) {
                ServiceResult.Error(errorMessage(ErrorType.UPLOAD))
            } else {
                val downloadUrl = uploadTask.storage.downloadUrl.await()
                if (downloadUrl == null) {
                    ServiceResult.Error(errorMessage(ErrorType.NOT_FOUND))
                } else {
                    ServiceResult.Success(downloadUrl.toString())
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return ServiceResult.Error(errorMessage())
        }
    }

}