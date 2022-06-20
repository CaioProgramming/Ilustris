package com.ilustris.app.view.dialog

import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_EMAIL
import android.content.Intent.EXTRA_SUBJECT
import android.net.Uri
import android.view.View
import com.ilustris.app.R
import com.ilustris.app.databinding.ContactDialogBinding
import com.ilustris.ui.alert.BaseAlert
import com.ilustris.ui.alert.DialogStyles


class ContactDialog(context: Context) :
    BaseAlert(context, R.layout.contact_dialog, DialogStyles.BOTTOM_NO_BORDER) {

    override fun View.configure() {
        ContactDialogBinding.bind(this).run {
            emailContact.setOnClickListener {
                openEmail()
            }

            phoneContact.setOnClickListener {
                openWhatsApp()
            }
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