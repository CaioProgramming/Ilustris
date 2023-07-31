package com.silent.ilustriscore.core.service

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.contract.DataException
import com.silent.ilustriscore.core.contract.ServiceContract
import com.silent.ilustriscore.core.contract.ServiceResult
import com.silent.ilustriscore.core.contract.ServiceSettings
import com.silent.ilustriscore.core.utilities.Ordering
import com.silent.ilustriscore.core.utilities.SEARCH_SUFFIX
import kotlinx.coroutines.tasks.await
import java.io.File

abstract class BaseService : ServiceContract, ServiceSettings {

    fun getUser() = currentUser()
    override val offlineEnabled = true
    protected val reference: CollectionReference by lazy {
        val fireStoreInstance = FirebaseFirestore.getInstance()
        val settings =
            FirebaseFirestoreSettings.Builder().setPersistenceEnabled(offlineEnabled).build()
        fireStoreInstance.firestoreSettings = settings
        return@lazy fireStoreInstance.collection(dataPath)
    }

    override suspend fun deleteData(id: String): ServiceResult<DataException, Boolean> {
        return try {
            logData("deleteData: deleting $id from collection $dataPath")
            if (requireAuth && currentUser() == null) return ServiceResult.Error(DataException.AUTH)
            reference.document(id).delete().await()
            ServiceResult.Success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            ServiceResult.Error(
                DataException.DELETE
            )
        }
    }

    override suspend fun query(
        query: String,
        field: String,
        limit: Long
    ): ServiceResult<DataException, ArrayList<BaseBean>> {
        logData("query: searching for $query at field $field on collection $dataPath with limit -> $limit")
        if (requireAuth && currentUser() == null) return ServiceResult.Error(DataException.AUTH)
        val query =
            reference.orderBy(field).startAt(query).endAt(query + SEARCH_SUFFIX).limit(limit).get()
                .await().documents
        return if (query.isNotEmpty()) {
            ServiceResult.Success(getDataList(query))
        } else {
            ServiceResult.Error(DataException.NOTFOUND)
        }
    }

    override suspend fun queryOnArray(
        query: String,
        field: String,
        limit: Long
    ): ServiceResult<DataException, ArrayList<BaseBean>> {
        logData("query: searching for $query at field $field on collection $dataPath with limit -> $limit")
        if (requireAuth && currentUser() == null) return ServiceResult.Error(DataException.AUTH)
        val queryTask =
            reference.whereArrayContains(field, query).get().await()
        return if (!queryTask.isEmpty) {
            ServiceResult.Success(getDataList(queryTask.documents))
        } else {
            ServiceResult.Error(DataException.NOTFOUND)
        }
    }


    suspend fun explicitSearch(
        query: String,
        field: String,
        orderBy: String = "id",
        ordering: Ordering = Ordering.DESCENDING,
        limit: Long = 500
    ): ServiceResult<DataException, ArrayList<BaseBean>> {
        if (requireAuth && currentUser() == null) return ServiceResult.Error(DataException.AUTH)
        logData("query: searching for $query at field $field on collection $dataPath with limit ordered by $orderBy($ordering) -> $limit ")
        val order =
            if (ordering == Ordering.DESCENDING) Query.Direction.DESCENDING else Query.Direction.ASCENDING
        val query = reference.whereEqualTo(field, query).limit(limit).orderBy(orderBy, order).get()
            .await().documents
        return if (query.isNotEmpty()) {
            ServiceResult.Success(getDataList(query))
        } else {
            ServiceResult.Error(DataException.NOTFOUND)
        }
    }


    override suspend fun getAllData(
        limit: Long,
        orderBy: String,
        ordering: Ordering
    ): ServiceResult<DataException, ArrayList<BaseBean>> {
        logData("getAllData: getting all data from collection $dataPath with limit -> $limit ordered by $orderBy($ordering)")
        if (requireAuth && currentUser() == null) return ServiceResult.Error(DataException.AUTH)
        val order =
            if (ordering == Ordering.DESCENDING) Query.Direction.DESCENDING else Query.Direction.ASCENDING
        val data = reference.limit(limit).orderBy(orderBy, order).get().await().documents
        logData("data received: $data")

        return if (data.isNotEmpty()) {
            ServiceResult.Success(getDataList(data))
        } else ServiceResult.Error(DataException.NOTFOUND)
    }

    override suspend fun getSingleData(id: String): ServiceResult<DataException, BaseBean> {
        if (requireAuth && currentUser() == null) return ServiceResult.Error(DataException.AUTH)
        val document = reference.document(id).get().await()
        return if (document != null) {
            logData("getSingleData: $document")
            val bean = deserializeDataSnapshot(document)
            if (bean != null) {
                ServiceResult.Success(bean)
            } else {
                ServiceResult.Error(DataException.NOTFOUND)
            }
        } else {
            ServiceResult.Error(DataException.NOTFOUND)
        }
    }

    override suspend fun editData(data: BaseBean): ServiceResult<DataException, BaseBean> {
        return try {
            if (requireAuth && currentUser() == null) return ServiceResult.Error(DataException.AUTH)
            val task = reference.document(data.id).set(data).await()
            logData("edited -> $data")
            ServiceResult.Success(data)
        } catch (e: Exception) {
            logData("update data Error!\n ${e.message}")
            e.printStackTrace()
            ServiceResult.Error(DataException.UPDATE)
        }
    }

    suspend fun editField(
        data: Any,
        id: String,
        field: String
    ): ServiceResult<DataException, String> {
        return try {
            if (requireAuth && currentUser() == null) return ServiceResult.Error(DataException.AUTH)
            val task = reference.document(id).update(field, data).await()
            if (task != null) {
                Log.i(javaClass.simpleName, "edit successful: $data")
                ServiceResult.Success("Dados atualizados")
            } else ServiceResult.Error(DataException.UPDATE)
        } catch (e: Exception) {
            e.printStackTrace()
            ServiceResult.Error(DataException.UPDATE)
        }
    }


    override suspend fun addData(data: BaseBean): ServiceResult<DataException, BaseBean> {
        return try {
            if (requireAuth && currentUser() == null) return ServiceResult.Error(DataException.AUTH)
            val task = reference.add(data).await()
            if (task != null) {
                Log.d(javaClass.simpleName, "data saved -> $data")
                ServiceResult.Success(data)
            } else ServiceResult.Error(DataException.SAVE)
        } catch (e: Exception) {
            e.printStackTrace()
            ServiceResult.Error(DataException.SAVE)
        }
    }


    suspend fun uploadToStorage(uri: String): ServiceResult<DataException, String> {
        try {
            if (requireAuth && currentUser() == null) return ServiceResult.Error(DataException.AUTH)
            val file = File(uri)
            val uriFile = Uri.fromFile(file)
            val iconRef = FirebaseStorage.getInstance().reference.child("$dataPath/${file.name}")
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