package com.silent.ilustriscore.core.contract

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.silent.ilustriscore.core.bean.BaseBean

interface ModelContract<T> where T : BaseBean {

    val dataPath: String
    fun deserializeDataSnapshot(dataSnapshot: DocumentSnapshot): T
    fun deserializeDataSnapshot(dataSnapshot: QueryDocumentSnapshot): T
}