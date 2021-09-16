package com.ilustris.app.view.adapter

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ilustris.app.appList
import com.ilustris.app.databinding.AppsCardLayoutBinding
import androidx.core.content.ContextCompat.startActivity

import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.ilustris.animations.fadeIn
import com.ilustris.animations.popIn
import com.ilustris.app.ADDNEWAPP
import com.ilustris.app.R
import com.silent.ilustriscore.core.utilities.gone


class AppsAdapter(var appList: appList, val addNewApp: () -> Unit) :
    RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {


    fun updateAdapter(newAppList: appList) {
        appList = newAppList
        notifyDataSetChanged()
    }

    inner class AppViewHolder(val appsCardLayoutBinding: AppsCardLayoutBinding) :
        RecyclerView.ViewHolder(appsCardLayoutBinding.root) {
        fun bind() {
            appList[adapterPosition].run {
                if (id != ADDNEWAPP) {
                    Glide.with(appsCardLayoutBinding.root.context)
                        .load(appIcon)
                        .into(appsCardLayoutBinding.appIcon)
                } else {
                    appsCardLayoutBinding.appIcon.setImageResource(R.drawable.ic_square_7)
                }
                appsCardLayoutBinding.appCard.setOnClickListener {
                    if (id != ADDNEWAPP) {
                        if (url.isNotEmpty()) {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            appsCardLayoutBinding.root.context.startActivity(browserIntent)
                        }
                    } else {
                        addNewApp.invoke()
                    }
                }
                appsCardLayoutBinding.appCard.fadeIn()
                if (id != ADDNEWAPP && url.isEmpty()) {
                    val matrix = ColorMatrix().apply {
                        setSaturation(0f)
                    }
                    val filter = ColorMatrixColorFilter(matrix)

                    appsCardLayoutBinding.appIcon.colorFilter = filter
                } else {
                    val matrix = ColorMatrix().apply {
                        setSaturation(1f)
                    }
                    val filter = ColorMatrixColorFilter(matrix)

                    appsCardLayoutBinding.appIcon.colorFilter = filter
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        return AppViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.apps_card_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = appList.size
}