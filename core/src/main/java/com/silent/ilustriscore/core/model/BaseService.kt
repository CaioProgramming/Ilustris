package com.silent.ilustriscore.core.model

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.contract.ServiceContract
import com.silent.ilustriscore.core.utilities.SEARCH_SUFFIX
import kotlinx.coroutines.tasks.await
import java.io.File

abstract class BaseService : ServiceContract {

    open var requireAuth: Boolean = false

    protected val reference: CollectionReference by lazy {
        FirebaseFirestore.getInstance().collection(dataPath)
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
        field: String
    ): ServiceResult<DataException, ArrayList<BaseBean>> {
        Log.i(javaClass.simpleName, "query: Buscando por $query em $field na collection $dataPath")
        if (requireAuth && currentUser() == null) return ServiceResult.Error(DataException.AUTH)
        val query = reference.orderBy(field).startAt(query).endAt(query + SEARCH_SUFFIX).get()
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
        field: String
    ): ServiceResult<DataException, ArrayList<BaseBean>> {
        if (requireAuth && currentUser() == null) return ServiceResult.Error(DataException.AUTH)
        Log.i(javaClass.simpleName, "query: Buscando por $query em $field na collection $dataPath")
        val query = reference.whereEqualTo(field, query).get().await().documents
        return if (query.isNotEmpty()) {
            ServiceResult.Success(getDataList(query))
        } else {
            ServiceResult.Error(DataException.NOTFOUND)
        }
    }

    override suspend fun getAllData(): ServiceResult<DataException, ArrayList<BaseBean>> {
        if (requireAuth && currentUser() == null) return ServiceResult.Error(DataException.AUTH)
        val data = reference.get().await().documents
        return if (data.isNotEmpty()) {
            Log.i(javaClass.simpleName, "getAllData: $data")
            ServiceResult.Success(getDataList(data))
        } else ServiceResult.Error(DataException.NOTFOUND)
    }

    override suspend fun getSingleData(id: String): ServiceResult<DataException, BaseBean> {
        if (requireAuth && currentUser() == null) return ServiceResult.Error(DataException.AUTH)
        val document = reference.document(id).get().await()
        return if (document != null) {
            Log.i(javaClass.simpleName, "getSingleData: $document")
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
                Log.i(javaClass.simpleName, "edited -> $data")
                ServiceResult.Success(data)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "update data Error!")
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