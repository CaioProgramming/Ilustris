package com.silent.ilustriscore.core.model

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.silent.ilustriscore.BuildConfig
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.contract.ServiceContract
import com.silent.ilustriscore.core.utilities.SEARCH_SUFFIX
import kotlinx.coroutines.tasks.await
import java.io.File

abstract class BaseService : ServiceContract {

    open var requireAuth: Boolean = false
    open var offlineEnabled: Boolean = false

    protected val reference: CollectionReference by lazy {
        val fireStoreInstance = FirebaseFirestore.getInstance()
        fireStoreInstance.firestoreSettings.apply {
            offlineEnabled = this@BaseService.offlineEnabled
        }
        return@lazy fireStoreInstance.collection(dataPath)
    }

    fun currentUser() = FirebaseAuth.getInstance().currentUser

    override suspend fun deleteData(id: String): ServiceResult<DataException, Boolean> {
        return try {
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
        logData("query: Searching for $query on field $field at collection $dataPath with limit -> $limit")
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

    override fun getDataList(querySnapshot: MutableList<DocumentSnapshot>): ArrayList<BaseBean> {
        return ArrayList<BaseBean>().apply {
            querySnapshot.forEach {
                deserializeDataSnapshot(it)?.let { it1 -> add(it1) }
            }
        }
    }

    suspend fun explicitSearch(
        query: String,
        field: String,
        limit: Long = 500
    ): ServiceResult<DataException, ArrayList<BaseBean>> {
        if (requireAuth && currentUser() == null) return ServiceResult.Error(DataException.AUTH)
        logData("query: Buscando por $query em $field na collection $dataPath with limit -> $limit")
        val query = reference.whereEqualTo(field, query).limit(limit).get().await().documents
        return if (query.isNotEmpty()) {
            ServiceResult.Success(getDataList(query))
        } else {
            ServiceResult.Error(DataException.NOTFOUND)
        }
    }

    private fun logData(logMessage: String) {
        if (BuildConfig.DEBUG) {
            Log.i(javaClass.simpleName, logMessage)
        }
    }

    override suspend fun getAllData(limit: Long): ServiceResult<DataException, ArrayList<BaseBean>> {
        if (requireAuth && currentUser() == null) return ServiceResult.Error(DataException.AUTH)
        val data = reference.limit(limit).get().await().documents
        return if (data.isNotEmpty()) {
            logData("get All Data from $dataPath limited to $limit -> \n $data")
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