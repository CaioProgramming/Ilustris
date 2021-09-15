package com.ilustris.app.view.adapter

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ilustris.app.appList
import com.ilustris.app.databinding.AppsCardLayoutBinding
import androidx.core.content.ContextCompat.startActivity

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.ilustris.animations.popIn
import com.ilustris.app.ADDNEWAPP
import com.ilustris.app.R


class AppsAdapter(val appList: appList, val addNewApp: () -> Unit) :
    RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {


    inner class AppViewHolder(val appsCardLayoutBinding: AppsCardLayoutBinding) :
        RecyclerView.ViewHolder(appsCardLayoutBinding.root) {
        fun bind() {
            appList[adapterPosition].run {
                Glide.with(appsCardLayoutBinding.root.context)
                    .load(if (id != ADDNEWAPP) appIcon else R.drawable.ic_round_add_24)
                    .into(appsCardLayoutBinding.appIcon)
                appsCardLayoutBinding.appCard.setOnClickListener {
                    if (id != ADDNEWAPP) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        appsCardLayoutBinding.root.context.startActivity(browserIntent)
                    } else {
                        addNewApp.invoke()
                    }
                }
                appsCardLayoutBinding.appCard.popIn()
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