package com.ilustris.app

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.service.BaseLiveService

class IlustrisLiveService : BaseLiveService() {

    override val dataPath = "Apps"
    override var requireAuth = true
    override val offlineEnabled = true

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