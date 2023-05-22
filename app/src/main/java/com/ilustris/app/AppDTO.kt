package com.ilustris.app

import com.silent.ilustriscore.core.bean.BaseBean


const val ADDNEWAPP = "ADDNEWAPP"
typealias appList = List<AppDTO>

data class AppDTO(
    override var id: String = "",
    var appIcon: String = "",
    var appName: String = "",
    var description: String = "",
    var url: String = "",
) : BaseBean(id)
