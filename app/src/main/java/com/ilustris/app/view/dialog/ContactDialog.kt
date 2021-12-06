package com.ilustris.app.view.dialog

import android.R.id
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_EMAIL
import android.content.Intent.EXTRA_SUBJECT
import android.view.View
import com.silent.ilustriscore.core.view.BaseAlert
import kotlinx.android.synthetic.main.contact_dialog.view.*
import android.R.id.message
import android.net.Uri
import com.ilustris.app.R
import com.silent.ilustriscore.core.utilities.DialogStyles


class ContactDialog(context: Context) :
    BaseAlert(context, R.layout.contact_dialog, DialogStyles.BOTTOM_NO_BORDER) {

    override fun View.configure() {
        email_contact.setOnClickListener {
            openEmail()
        }

        phone_contact.setOnClickListener {
            openWhatsApp()
        }
    }

    private fun openWhatsApp() {
        val uri: Uri = Uri.parse("https://api.whatsapp.com/send?phone=" + "+5511965766738")
        val sendIntent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(sendIntent)
    }

    private fun openEmail() {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "plain/text"
            putExtra(EXTRA_EMAIL, "ilustrisinc@gmail.com")
            putExtra(EXTRA_SUBJECT, "Solicitação de app")
        }
        context.startActivity(emailIntent)
    }
}