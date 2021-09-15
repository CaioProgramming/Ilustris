package com.ilustris.app.view

import com.ilustris.animations.fadeIn
import com.ilustris.app.ADDNEWAPP
import com.ilustris.app.AppDTO
import com.ilustris.app.IlustrisPresenter
import com.ilustris.app.databinding.ActivityMainBinding
import com.ilustris.app.view.adapter.AppsAdapter
import com.ilustris.app.view.dialog.NewAppDialog
import com.silent.ilustriscore.core.utilities.delayedFunction
import com.silent.ilustriscore.core.utilities.visible
import com.silent.ilustriscore.core.view.BaseView

class MainActBinder(override val viewBind: ActivityMainBinding) : BaseView<AppDTO>() {

    override val presenter = IlustrisPresenter(this)

    override fun initView() {
        viewBind.starAnimation.playAnimation()
        presenter.loadData()
    }

    override fun showListData(list: List<AppDTO>) {
        super.showListData(list)
        if (viewBind.appsRecyclerView.childCount == 0) {
            delayedFunction(3000) {
                viewBind.appbar.setExpanded(false, true)
            }
        }
        val appList = ArrayList(list)
        appList.add(AppDTO(id = ADDNEWAPP))
        viewBind.appsRecyclerView.adapter = AppsAdapter(appList) {
            NewAppDialog(context) { newApp ->
                presenter.uploadFile(newApp.appIcon) { iconUrl ->
                    newApp.appIcon = iconUrl
                    presenter.saveData(newApp)
                }
            }.buildDialog()
        }

    }

}