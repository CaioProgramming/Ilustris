package com.silent.ilustriscore.core.service

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.contract.DataError
import com.silent.ilustriscore.core.contract.PagingResult
import com.silent.ilustriscore.core.contract.ServiceContract
import com.silent.ilustriscore.core.contract.ServiceResult
import com.silent.ilustriscore.core.contract.ServiceSettings
import com.silent.ilustriscore.core.utilities.Ordering
import com.silent.ilustriscore.core.utilities.SEARCH_SUFFIX
import kotlinx.coroutines.tasks.await
import java.io.File

abstract class BaseService : ServiceContract, ServiceSettings {

    override val offlineEnabled = true

    private fun authError(): ServiceResult.Error<DataError> {
        return ServiceResult.Error(DataError.Auth)
    }

    protected val reference: CollectionReference by lazy {
        val fireStoreInstance = FirebaseFirestore.getInstance()
        val settings =
            FirebaseFirestoreSettings.Builder().setPersistenceEnabled(offlineEnabled).build()
        fireStoreInstance.firestoreSettings = settings
        return@lazy fireStoreInstance.collection(dataPath)
    }

    override suspend fun deleteData(id: String): ServiceResult<DataError, Boolean> {
        return try {
            logData("deleteData: deleting $id from collection $dataPath")
            if (requireAuth && getCurrentUser() == null) return authError()
            reference.document(id).delete().await()
            ServiceResult.Success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            ServiceResult.Error(DataError.Delete)
        }
    }

    override suspend fun query(
        query: String,
        field: String,
        limit: Long
    ): ServiceResult<DataError, ArrayList<BaseBean>> {
        return try {
            logData("query: searching for $query at field $field on collection $dataPath with limit -> $limit")
            if (requireAuth && getCurrentUser() == null) return authError()
            val query =
                reference.orderBy(field).startAt(query).endAt(query + SEARCH_SUFFIX).limit(limit)
                    .get()
                    .await().documents
            if (query.isNotEmpty()) {
                ServiceResult.Success(getDataList(query))
            } else {
                ServiceResult.Error(DataError.NotFound)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ServiceResult.Error(DataError.Unknown(e.message))
        }
    }

    override suspend fun queryOnArray(
        query: String,
        field: String,
        limit: Long
    ): ServiceResult<DataError, ArrayList<BaseBean>> {
        logData("query: searching for $query at field $field on collection $dataPath with limit -> $limit")
        if (requireAuth && getCurrentUser() == null) return authError()
        val queryTask =
            reference.whereArrayContains(field, query).get().await()
        return if (!queryTask.isEmpty) {
            ServiceResult.Success(getDataList(queryTask.documents))
        } else {
            ServiceResult.Error(DataError.NotFound)
        }
    }


    suspend fun explicitSearch(
        query: String,
        field: String,
        orderBy: String = "id",
        ordering: Ordering = Ordering.DESCENDING,
        limit: Long = 500
    ): ServiceResult<DataError, ArrayList<BaseBean>> {
        if (requireAuth && getCurrentUser() == null) return authError()
        logData("query: searching for $query at field $field on collection $dataPath with limit ordered by $orderBy($ordering) -> $limit ")
        val order =
            if (ordering == Ordering.DESCENDING) Query.Direction.DESCENDING else Query.Direction.ASCENDING
        val query = reference.whereEqualTo(field, query).limit(limit).orderBy(orderBy, order).get()
            .await().documents
        return if (query.isNotEmpty()) {
            ServiceResult.Success(getDataList(query))
        } else {
            ServiceResult.Error(DataError.NotFound)
        }
    }


    override suspend fun getAllData(
        limit: Long,
        orderBy: String,
        ordering: Ordering
    ): ServiceResult<DataError, ArrayList<BaseBean>> {
        logData("getAllData: getting all data from collection $dataPath with limit -> $limit ordered by $orderBy($ordering)")
        if (requireAuth && getCurrentUser() == null) return authError()
        val order =
            if (ordering == Ordering.DESCENDING) Query.Direction.DESCENDING else Query.Direction.ASCENDING
        val data = reference.limit(limit).orderBy(orderBy, order).get().await().documents
        logData("data received: $data")

        return if (data.isNotEmpty()) {
            ServiceResult.Success(getDataList(data))
        } else ServiceResult.Error(DataError.NotFound)
    }


    override suspend fun getPagingData(
        pageSize: Long,
        lastDocument: DocumentSnapshot?,
        orderBy: String?,
        ordering: Ordering
    ): ServiceResult<DataError, PagingResult<BaseBean>> {
        logData("getPagingData: getting all data from collection $dataPath with limit -> $pageSize ordered by $orderBy($ordering)")
        if (requireAuth && getCurrentUser() == null) return authError()
        val order =
            if (ordering == Ordering.DESCENDING) Query.Direction.DESCENDING else Query.Direction.ASCENDING
        val data = reference.apply {
            orderBy?.let {
                orderBy(it, order)
            }
            lastDocument?.let {
                startAfter(it)
            }
        }.limit(pageSize).get().await().documents
        logData("data received: $data")

        return if (data.isNotEmpty()) {
            ServiceResult.Success(getPageResult(getDataList(data), data.last()))
        } else ServiceResult.Error(DataError.NotFound)
    }

    private fun getPageResult(dataList: ArrayList<BaseBean>, lastDocument: DocumentSnapshot) =
        PagingResult(dataList, lastDocument)

    override suspend fun getSingleData(id: String): ServiceResult<DataError, BaseBean> {
        if (requireAuth && getCurrentUser() == null) return authError()
        val document = reference.document(id).get().await()
        return if (document != null) {
            logData("getSingleData: $document")
            val bean = deserializeDataSnapshot(document)
            if (bean != null) {
                ServiceResult.Success(bean)
            } else {
                ServiceResult.Error(DataError.NotFound)
            }
        } else {
            ServiceResult.Error(DataError.NotFound)
        }
    }

    override suspend fun editData(data: BaseBean): ServiceResult<DataError, BaseBean> {
        return try {
            if (requireAuth && getCurrentUser() == null) return authError()
            val task = reference.document(data.id).set(data).await()
            logData("edited -> $data")
            ServiceResult.Success(data)
        } catch (e: Exception) {
            logData("update data Error!\n ${e.message}")
            e.printStackTrace()
            ServiceResult.Error(DataError.Update)
        }
    }

    suspend fun editField(
        data: Any,
        id: String,
        field: String
    ): ServiceResult<DataError, String> {
        return try {
            if (requireAuth && getCurrentUser() == null) return authError()
            reference.document(id).update(field, data).await()
            ServiceResult.Success("Dados atualizados")
        } catch (e: Exception) {
            e.printStackTrace()
            ServiceResult.Error(DataError.Update)
        }
    }


    override suspend fun addData(data: BaseBean): ServiceResult<DataError, BaseBean> {
        return try {
            if (requireAuth && getCurrentUser() == null) return authError()
            val task = reference.add(data).await()
            if (task != null) {
                Log.d(javaClass.simpleName, "data saved -> $data")
                ServiceResult.Success(data)
            } else ServiceResult.Error(DataError.Save)
        } catch (e: Exception) {
            e.printStackTrace()
            ServiceResult.Error(DataError.Save)
        }
    }


    suspend fun uploadToStorage(uri: String): ServiceResult<DataError, String> {
        try {
            if (requireAuth && getCurrentUser() == null) return authError()
            val file = File(uri)
            val uriFile = Uri.fromFile(file)
            val iconRef = FirebaseStorage.getInstance().reference.child("$dataPath/${file.name}")
            val uploadTask = iconRef.putFile(uriFile).await()
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
            return ServiceResult.Error(DataError.Upload)
        }
    }

}