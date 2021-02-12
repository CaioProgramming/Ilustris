package com.creat.motiv.contract

import android.content.Context
import androidx.databinding.ViewDataBinding
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.model.DTOMessage
import com.silent.ilustriscore.core.presenter.BasePresenter


/**
 * @Author Kotlin MVP Plugin
 * @Date 2019/10/15
 * @Description input description
 **/
interface ViewContract<T> where  T : BaseBean {

    val context: Context
    val viewBind: ViewDataBinding
    fun presenter(): BasePresenter<T>
    fun onLoading()
    fun onLoadFinish()
    fun error(message: String)
    fun success(message: String)
    fun initView()
    fun showListData(list: List<T>)
    fun showData(data: T)
    fun getCallBack(dtoMessage: DTOMessage)


}