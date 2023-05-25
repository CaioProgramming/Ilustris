package com.ilustris.app.view.adapter

import android.content.Intent
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
import com.ilustris.app.databinding.AppsCardLayoutBinding
import com.ilustris.ui.extensions.setSaturation


class AppsAdapter(
    var appList: appList, val addNewApp: () -> Unit,
    val editApp: (AppDTO) -> Unit
) :
    RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {


    inner class AppViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        private val appsCardLayoutBinding = AppsCardLayoutBinding.bind(itemView)

        fun bind() {
            val app = appList[adapterPosition]
            appsCardLayoutBinding.run {
                if (app.id != ADDNEWAPP) {
                    Glide.with(itemView.context)
                        .load(app.icon)
                        .into(appLogoImageView)
                    appCard.setOnClickListener {
                        if (app.url.isNotEmpty()) {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(app.url))
                            itemView.context.startActivity(browserIntent)
                        }
                    }
                    appCard.setOnLongClickListener {
                        editApp(app)
                        false
                    }
                    if (app.url.isEmpty()) {
                        appLogoImageView.setSaturation(0f)
                    } else {
                        appLogoImageView.setSaturation(1f)
                    }
                } else {
                    appCard.setOnClickListener {
                        addNewApp()
                    }
                    appLogoImageView.setImageResource(R.drawable.ic_round_star_border_24)
                    appCard.setCardBackgroundColor(itemView.context.getColor(R.color.material_grey200))
                }
                appCard.fadeIn()
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