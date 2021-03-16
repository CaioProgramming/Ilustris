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

    fun addData(data: T, forcedID: String? = null)

    fun editData(data: T)

    fun deleteData(id: String)

    fun query(query: String, field: String)

    fun getAllData()

    fun getSingleData(id: String)


}