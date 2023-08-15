package com.silent.ilustriscore.core.contract

import com.google.firebase.firestore.DocumentSnapshot
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.utilities.Ordering

interface ServiceContract {


    suspend fun addData(data: BaseBean): ServiceResult<DataError, BaseBean>
    suspend fun editData(data: BaseBean): ServiceResult<DataError, BaseBean>
    suspend fun deleteData(id: String): ServiceResult<DataError, Boolean>
    suspend fun query(
        query: String,
        field: String,
        limit: Long = 500,
    ): ServiceResult<DataError, ArrayList<BaseBean>>

    suspend fun queryOnArray(
        query: String,
        field: String,
        limit: Long = 500,
    ): ServiceResult<DataError, ArrayList<BaseBean>>

    suspend fun getAllData(
        limit: Long = 500,
        orderBy: String = "id",
        ordering: Ordering = Ordering.DESCENDING
    ): ServiceResult<DataError, ArrayList<BaseBean>>

    suspend fun getPagingData(
        pageSize: Long = 100,
        lastDocument: DocumentSnapshot? = null,
        orderBy: String?,
        ordering: Ordering = Ordering.DESCENDING
    ): ServiceResult<DataError, PagingResult<BaseBean>>

    suspend fun getSingleData(id: String): ServiceResult<DataError, BaseBean>


}