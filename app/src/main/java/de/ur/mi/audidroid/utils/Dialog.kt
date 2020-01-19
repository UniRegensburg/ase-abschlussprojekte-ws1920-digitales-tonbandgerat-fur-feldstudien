package de.ur.mi.audidroid.utils

import android.app.AlertDialog
import android.content.Context
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.viewmodels.RecordViewModel

object Dialog {

    fun createDialog(context: Context, layoutId: Int? = null, textId: Int? = null) {
        val builder = AlertDialog.Builder(context)
        if (textId != null) {
            val permissionHelper = PermissionHelper(context)
            builder.setTitle(R.string.permission_title)
            builder.setMessage(textId)
            builder.setPositiveButton(
                R.string.permission_button
            ) { _, _ ->
                permissionHelper.makeRequest()
            }
        }
        if (layoutId != null) {
            val recordViewModel = RecordViewModel()
            builder.setView(layoutId)
            builder.setPositiveButton(context.getString(R.string.dialog_save_button_text)) { _, _ ->
                recordViewModel.fromDialog(context)
            }
        }
        val dialog = builder.create()
        dialog.show()
    }
}
