package com.silent.ilustriscore.core.contract

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

interface StorageSettings {

    val dataPath: String
    val requireAuth: Boolean
    fun storageInstance() = FirebaseStorage.getInstance()
    fun storageReference() = FirebaseStorage.getInstance().reference.child(dataPath)
    fun currentUser() = FirebaseAuth.getInstance().currentUser
}