package com.silent.ilustriscore.core.contract

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.model.DataException
import com.silent.ilustriscore.core.model.ServiceResult

interface ServiceContract {

    val dataPath: String
    fun deserializeDataSnapshot(dataSnapshot: DocumentSnapshot): BaseBean?
    fun deserializeDataSnapshot(dataSnapshot: QueryDocumentSnapshot): BaseBean
    fun getDataList(querySnapshot: MutableList<DocumentSnapshot>): ArrayList<BaseBean>
    suspend fun addData(data: BaseBean): ServiceResult<DataException, BaseBean>
    suspend fun editData(data: BaseBean): ServiceResult<DataException, BaseBean>
    suspend fun deleteData(id: String): ServiceResult<DataException, Boolean>
    suspend fun query(
        query: String,
        field: String,
        limit: Long = 500
    ): ServiceResult<DataException, ArrayList<BaseBean>>

    suspend fun getAllData(limit: Long = 500): ServiceResult<DataException, ArrayList<BaseBean>>
    suspend fun getSingleData(id: String): ServiceResult<DataException, BaseBean>
}