package com.silent.ilustriscore.core.service

import android.util.Log
import com.google.firebase.firestore.Query
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.contract.DataError
import com.silent.ilustriscore.core.contract.LiveServiceContract
import com.silent.ilustriscore.core.contract.ServiceResult
import com.silent.ilustriscore.core.contract.ServiceSettings
import com.silent.ilustriscore.core.utilities.Ordering
import com.silent.ilustriscore.core.utilities.SEARCH_SUFFIX
import com.silent.ilustriscore.core.utilities.authError
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

abstract class BaseLiveService : LiveServiceContract, ServiceSettings {


    override suspend fun deleteData(id: String): ServiceResult<DataError, Boolean> {
        return try {
            if (requireAuth && getCurrentUser() == null) return authError()
            fireStoreReference().document(id).delete().await()
            ServiceResult.Success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            ServiceResult.Error(
                DataError.Unknown(e.message)
            )
        }
    }

    override suspend fun query(
        query: String,
        field: String,
        limit: Long
    ): Flow<ServiceResult<DataError, ArrayList<BaseBean>>> = callbackFlow {
        val reference = fireStoreReference()
        if (requireAuth && getCurrentUser() == null) {
            send(authError())
            cancel("User not logged in")
            awaitClose()
        }

        reference.limit(limit).orderBy(field).startAt(query).endAt(query + SEARCH_SUFFIX)
            .limit(limit).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySendBlocking(ServiceResult.Error(DataError.Unknown(error.message)))
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
    ): Flow<ServiceResult<DataError, ArrayList<BaseBean>>> = callbackFlow {
        val reference = fireStoreReference()
        if (requireAuth && getCurrentUser() == null) {
            send(authError())
            cancel("User not logged in")
            awaitClose()
        }

        reference.whereArrayContains(field, query).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySendBlocking(ServiceResult.Error(DataError.Unknown(error.message)))
            } else {
                snapshot?.let {
                    trySendBlocking(ServiceResult.Success(getDataList(it.documents)))
                }
            }
        }
        awaitClose()
    }


    override suspend fun editData(data: BaseBean): ServiceResult<DataError, BaseBean> {
        return try {
            if (requireAuth && getCurrentUser() == null) return authError()
            fireStoreReference().document(data.id).set(data).await()
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
            fireStoreReference().document(id).update(field, data).await()
            ServiceResult.Success("Dados atualizados")
        } catch (e: Exception) {
            e.printStackTrace()
            ServiceResult.Error(DataError.Unknown(e.message))
        }
    }


    override suspend fun addData(data: BaseBean): ServiceResult<DataError, BaseBean> {
        return try {
            if (requireAuth && getCurrentUser() == null) return authError()
            val task = fireStoreReference().add(data).await()
            if (task != null) {
                Log.d(javaClass.simpleName, "data saved -> $data")
                ServiceResult.Success(data)
            } else ServiceResult.Error(DataError.Save)
        } catch (e: Exception) {
            e.printStackTrace()
            ServiceResult.Error(DataError.Unknown(e.message))
        }
    }

    override fun getAllData(
        limit: Long,
        orderBy: String,
        ordering: Ordering
    ): Flow<ServiceResult<DataError, ArrayList<BaseBean>>> = callbackFlow {
        val reference = fireStoreReference()
        if (requireAuth && getCurrentUser() == null) {
            send(authError())
            cancel("User not logged in")
            awaitClose()
        }
        val order =
            if (ordering == Ordering.DESCENDING) Query.Direction.DESCENDING else Query.Direction.ASCENDING

        reference.limit(limit).orderBy(orderBy, order).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySendBlocking(ServiceResult.Error(DataError.Unknown(error.message)))
            } else {
                snapshot?.let {
                    trySendBlocking(ServiceResult.Success(getDataList(it.documents)))
                }
            }
        }

        awaitClose()

    }

    override suspend fun getSingleData(id: String): Flow<ServiceResult<DataError, BaseBean>> =
        callbackFlow {
            if (requireAuth && getCurrentUser() == null) {
                send(authError())
                cancel("User not logged in")
                awaitClose()
            }
            fireStoreReference().document(id).get().addOnCompleteListener { document ->
                logData("getSingleData: ${document.result}")
                val data = deserializeDataSnapshot(document.result)
                if (data == null) {
                    trySendBlocking(ServiceResult.Error(DataError.NotFound))
                } else {
                    trySendBlocking(ServiceResult.Success(data))
                }
            }
            awaitClose()
        }

}