package com.silent.ilustriscore.core.service

import android.util.Log
import com.google.firebase.firestore.Query
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.contract.DataException
import com.silent.ilustriscore.core.contract.LiveServiceContract
import com.silent.ilustriscore.core.contract.ServiceResult
import com.silent.ilustriscore.core.contract.ServiceSettings
import com.silent.ilustriscore.core.utilities.Ordering
import com.silent.ilustriscore.core.utilities.SEARCH_SUFFIX
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

abstract class BaseLiveService : LiveServiceContract, ServiceSettings {

    fun user() = currentUser()

    override suspend fun deleteData(id: String): ServiceResult<DataException, Boolean> {
        return try {
            if (requireAuth && currentUser() == null) return ServiceResult.Error(DataException.AUTH)
            fireStoreReference().document(id).delete().await()
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
    ): Flow<ServiceResult<DataException, ArrayList<BaseBean>>> = callbackFlow {
        val reference = fireStoreReference()
        if (requireAuth && currentUser() == null) {
            send(ServiceResult.Error(DataException.AUTH))
            cancel("User not logged in")
            awaitClose()
        }

        reference.limit(limit).orderBy(field).startAt(query).endAt(query + SEARCH_SUFFIX)
            .limit(limit).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySendBlocking(ServiceResult.Error(DataException.UNKNOWN))
            } else {
                snapshot?.let {
                    trySendBlocking(ServiceResult.Success(getDataList(it.documents)))
                }
            }
        }
        awaitClose()
    }


    override suspend fun queryOnArray(
        query: String,
        field: String,
        limit: Long
    ): Flow<ServiceResult<DataException, ArrayList<BaseBean>>> = callbackFlow {
        val reference = fireStoreReference()
        if (requireAuth && currentUser() == null) {
            send(ServiceResult.Error(DataException.AUTH))
            cancel("User not logged in")
            awaitClose()
        }

        reference.whereArrayContains(field, query).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySendBlocking(ServiceResult.Error(DataException.UNKNOWN))
            } else {
                snapshot?.let {
                    trySendBlocking(ServiceResult.Success(getDataList(it.documents)))
                }
            }
        }

        awaitClose()

    }


    override suspend fun editData(data: BaseBean): ServiceResult<DataException, BaseBean> {
        return try {
            if (requireAuth && currentUser() == null) return ServiceResult.Error(DataException.AUTH)
            val task = fireStoreReference().document(data.id).set(data).await()
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
            val task = fireStoreReference().document(id).update(field, data).await()
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
            val task = fireStoreReference().add(data).await()
            if (task != null) {
                Log.d(javaClass.simpleName, "data saved -> $data")
                ServiceResult.Success(data)
            } else ServiceResult.Error(DataException.SAVE)
        } catch (e: Exception) {
            e.printStackTrace()
            ServiceResult.Error(DataException.SAVE)
        }
    }

    override fun getAllData(
        limit: Long,
        orderBy: String,
        ordering: Ordering
    ): Flow<ServiceResult<DataException, ArrayList<BaseBean>>> = callbackFlow {
        val reference = fireStoreReference()
        if (requireAuth && currentUser() == null) {
            send(ServiceResult.Error(DataException.AUTH))
            cancel("User not logged in")
            awaitClose()
        }
        val order =
            if (ordering == Ordering.DESCENDING) Query.Direction.DESCENDING else Query.Direction.ASCENDING

        reference.limit(limit).orderBy(orderBy, order).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySendBlocking(ServiceResult.Error(DataException.UNKNOWN))
            } else {
                snapshot?.let {
                    trySendBlocking(ServiceResult.Success(getDataList(it.documents)))
                }
            }
        }

        awaitClose()

    }

    override suspend fun getSingleData(id: String): Flow<ServiceResult<DataException, BaseBean>> =
        callbackFlow {
            if (requireAuth && currentUser() == null) {
                send(ServiceResult.Error(DataException.AUTH))
                cancel("User not logged in")
                awaitClose()
            }
            fireStoreReference().document(id).get().addOnCompleteListener { document ->
                logData("getSingleData: ${document.result}")
                val data = deserializeDataSnapshot(document.result)
                if (data == null) {
                    trySendBlocking(ServiceResult.Error(DataException.NOTFOUND))
                } else {
                    trySendBlocking(ServiceResult.Success(data))
                }
            }
            awaitClose()
        }

}