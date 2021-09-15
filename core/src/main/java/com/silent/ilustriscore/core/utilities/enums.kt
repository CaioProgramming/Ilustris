package com.silent.ilustriscore.core.utilities

import com.silent.ilustriscore.R


enum class MessageType {
    ERROR, SUCCESS, WARNING, INFO
}

enum class OperationType {
    DATA_SAVED, DATA_UPDATED, DELETE, DATA_RETRIEVED, UNKNOWN
}

enum class DialogStyles(val resource: Int) {
    DEFAULT_NO_BORDER(R.style.Dialog_No_Border),
    BOTTOM_NO_BORDER(R.style.Bottom_Dialog_No_Border),
    FULL_SCREEN(R.style.Full_Screen_Dialog)
}



