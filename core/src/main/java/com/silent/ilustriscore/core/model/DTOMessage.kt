package com.silent.ilustriscore.core.model

import com.silent.ilustriscore.core.utilities.MessageType
import com.silent.ilustriscore.core.utilities.OperationType

data class DTOMessage(
    val message: String,
    val type: MessageType,
    val operationType: OperationType = OperationType.UNKNOW
)