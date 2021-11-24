package com.silent.ilustriscore.core.contract

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.model.DataException
import com.silent.ilustriscore.core.model.ServiceResult

interface ServiceContract<T> where T : BaseBean {

    val dataPath: String
    fun deserializeDataSnapshot(dataSnapshot: DocumentSnapshot): T
    fun deserializeDataSnapshot(dataSnapshot: QueryDocumentSnapshot): T
    suspend fun addData(data: T): ServiceResult<DataException, T>
    suspend fun editData(data: T): ServiceResult<DataException, T>
    suspend fun deleteData(id: String): ServiceResult<DataException, Boolean>
    suspend fun query(query: String, field: String): ServiceResult<DataException, ArrayList<T>>
    suspend fun getAllData(): ServiceResult<DataException, ArrayList<T>>
    suspend fun getSingleData(id: String): ServiceResult<DataException, T>
}