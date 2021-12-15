package com.ilustris.app.view.adapter

import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ilustris.animations.fadeIn
import com.ilustris.app.ADDNEWAPP
import com.ilustris.app.AppDTO
import com.ilustris.app.R
import com.ilustris.app.appList
import kotlinx.android.synthetic.main.apps_card_layout.view.*


class AppsAdapter(
    var appList: appList, val addNewApp: () -> Unit,
    val deleteApp: (AppDTO) -> Unit, val editApp: (AppDTO) -> Unit
) :
    RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {


    inner class AppViewHolder(itemview: View) :
        RecyclerView.ViewHolder(itemview) {
        fun bind() {
            appList[adapterPosition].run {
                if (id != ADDNEWAPP) {
                    Glide.with(itemView.context)
                        .load(appIcon)
                        .into(itemView.appLogoImageView)
                } else {
                    itemView.appLogoImageView.setImageResource(R.drawable.ic_square_7)
                }
                itemView.appCard.setOnClickListener {
                    if (id != ADDNEWAPP) {
                        if (url.isNotEmpty()) {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            itemView.context.startActivity(browserIntent)
                        }
                    } else {
                        addNewApp.invoke()
                    }
                }
                if (id != ADDNEWAPP) {
                    itemView.appCard.setOnLongClickListener {
                        deleteApp(this)
                        false
                    }
                    itemView.appCard.setOnDragListener { v, event ->
                        editApp(this)
                        false
                    }
                }
                itemView.appCard.fadeIn()
                if (id != ADDNEWAPP && url.isEmpty()) {
                    val matrix = ColorMatrix().apply {
                        setSaturation(0f)
                    }
                    val filter = ColorMatrixColorFilter(matrix)

                    itemView.appLogoImageView.colorFilter = filter
                } else {
                    val matrix = ColorMatrix().apply {
                        setSaturation(1f)
                    }
                    val filter = ColorMatrixColorFilter(matrix)

                    itemView.appLogoImageView.colorFilter = filter
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        return AppViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.apps_card_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = appList.size
}