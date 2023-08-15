package com.silent.ilustriscore.core.contract

import com.google.firebase.firestore.DocumentSnapshot
import com.silent.ilustriscore.core.bean.BaseBean

data class PagingResult<T>(
    val page: List<T>,
    val lastDocument: DocumentSnapshot
) where T : BaseBean
