package com.silent.ilustriscore.core.contract

import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.utilities.Ordering

interface ServiceContract {


    suspend fun addData(data: BaseBean): ServiceResult<DataException, BaseBean>
    suspend fun editData(data: BaseBean): ServiceResult<DataException, BaseBean>
    suspend fun deleteData(id: String): ServiceResult<DataException, Boolean>
    suspend fun query(
        query: String,
        field: String,
        limit: Long = 500,
    ): ServiceResult<DataException, ArrayList<BaseBean>>

    suspend fun queryOnArray(
        query: String,
        field: String,
        limit: Long = 500,
    ): ServiceResult<DataException, ArrayList<BaseBean>>

    suspend fun getAllData(
        limit: Long = 500,
        orderBy: String = "id",
        ordering: Ordering = Ordering.DESCENDING
    ): ServiceResult<DataException, ArrayList<BaseBean>>

    suspend fun getSingleData(id: String): ServiceResult<DataException, BaseBean>


}