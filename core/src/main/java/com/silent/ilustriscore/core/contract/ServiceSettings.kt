package com.silent.ilustriscore.core.contract

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.BuildConfig
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.silent.ilustriscore.core.bean.BaseBean

interface ServiceSettings {

    val dataPath: String
    val requireAuth: Boolean
    val offlineEnabled: Boolean

    fun getCurrentUser() = FirebaseAuth.getInstance().currentUser

    fun fireStoreReference(): CollectionReference {
        val fireStoreInstance = FirebaseFirestore.getInstance()
        val settings =
            FirebaseFirestoreSettings.Builder().setPersistenceEnabled(offlineEnabled).build()
        fireStoreInstance.firestoreSettings = settings
        return fireStoreInstance.collection(dataPath)
    }

    fun isDebug() = BuildConfig.DEBUG


    fun getDataList(querySnapshot: MutableList<DocumentSnapshot>): ArrayList<BaseBean> {
        return ArrayList<BaseBean>().apply {
            querySnapshot.forEach {
                deserializeDataSnapshot(it)?.let { it1 -> add(it1) }
            }
        }
    }

    fun deserializeDataSnapshot(dataSnapshot: DocumentSnapshot): BaseBean?
    fun deserializeDataSnapshot(dataSnapshot: QueryDocumentSnapshot): BaseBean

    fun logData(logMessage: String) {
        if (isDebug()) {
            Log.i(javaClass.simpleName, logMessage)
        }
    }

}