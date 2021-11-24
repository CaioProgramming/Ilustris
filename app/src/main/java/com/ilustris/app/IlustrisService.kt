package com.ilustris.app

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.silent.ilustriscore.core.model.BaseService

class IlustrisService : BaseService<AppDTO>() {
    override val dataPath = "Apps"

    override fun deserializeDataSnapshot(dataSnapshot: DocumentSnapshot): AppDTO {
        return dataSnapshot.toObject(AppDTO::class.java)!!.apply {
            id = dataSnapshot.id
        }
    }

    override fun deserializeDataSnapshot(dataSnapshot: QueryDocumentSnapshot): AppDTO {
        return dataSnapshot.toObject(AppDTO::class.java).apply {
            id = dataSnapshot.id
        }
    }
}