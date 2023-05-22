package com.ilustris.app

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.service.BaseService

class IlustrisService : BaseService() {

    override val dataPath = "Apps"
    override var requireAuth = true

    override fun deserializeDataSnapshot(dataSnapshot: DocumentSnapshot): BaseBean? {
        return dataSnapshot.toObject(AppDTO::class.java)?.apply {
            id = dataSnapshot.id
        }
    }

    override fun deserializeDataSnapshot(dataSnapshot: QueryDocumentSnapshot): BaseBean {
        return dataSnapshot.toObject(AppDTO::class.java).apply {
            id = dataSnapshot.id
        }
    }
}