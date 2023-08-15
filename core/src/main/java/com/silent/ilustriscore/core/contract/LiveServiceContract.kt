package com.silent.ilustriscore.core.contract

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.utilities.Ordering
import kotlinx.coroutines.flow.Flow

interface LiveServiceContract {

    val dataPath: String
    fun deserializeDataSnapshot(dataSnapshot: DocumentSnapshot): BaseBean?
    fun deserializeDataSnapshot(dataSnapshot: QueryDocumentSnapshot): BaseBean

    suspend fun addData(data: BaseBean): ServiceResult<DataError, BaseBean>
    suspend fun editData(data: BaseBean): ServiceResult<DataError, BaseBean>
    suspend fun deleteData(id: String): ServiceResult<DataError, Boolean>

    suspend fun query(
        query: String,
        field: String,
        limit: Long = 500,
    ): Flow<ServiceResult<DataError, ArrayList<BaseBean>>>

    suspend fun queryOnArray(
        query: String,
        field: String,
        limit: Long = 500,
    ): Flow<ServiceResult<DataError, ArrayList<BaseBean>>>

    fun getAllData(
        limit: Long = 500, orderBy: String = "id", ordering: Ordering = Ordering.DESCENDING,

        ): Flow<ServiceResult<DataError, ArrayList<BaseBean>>>

    suspend fun getSingleData(id: String): Flow<ServiceResult<DataError, BaseBean>>
}