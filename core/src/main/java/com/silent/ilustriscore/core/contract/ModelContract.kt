package com.silent.ilustriscore.core.contract

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.model.DataException
import com.silent.ilustriscore.core.presenter.BasePresenter

interface ModelContract<T> where T : BaseBean {

    val path: String
    fun deserializeDataSnapshot(dataSnapshot: DocumentSnapshot): T
    fun deserializeDataSnapshot(dataSnapshot: QueryDocumentSnapshot): T

    @Throws(DataException::class)
    fun addData(data: T, forcedID: String? = null)

    @Throws(DataException::class)
    fun editData(data: T)

    @Throws(DataException::class)
    fun deleteData(id: String)

    @Throws(DataException::class)
    fun query(query: String, field: String)

    @Throws(DataException::class)
    fun getAllData()

    @Throws(DataException::class)
    fun getSingleData(id: String)


}