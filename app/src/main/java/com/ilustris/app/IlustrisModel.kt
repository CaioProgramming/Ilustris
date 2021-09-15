package com.ilustris.app

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.silent.ilustriscore.core.model.BaseModel
import com.silent.ilustriscore.core.presenter.BasePresenter

class IlustrisModel(presenter: BasePresenter<AppDTO>) : BaseModel<AppDTO>(presenter) {
    override val path = "Apps"

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