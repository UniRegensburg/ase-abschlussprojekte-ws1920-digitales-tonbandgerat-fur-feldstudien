package de.ur.mi.audidroid.utils

import android.app.AlertDialog
import android.content.Context
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.viewmodels.RecordViewModel

object Dialog {

    fun createDialog(context: Context, layoutId: Int? = null, textId: Int? = null) {
        val builder = AlertDialog.Builder(context)
        if (textId != null) {
            builder.setTitle(R.string.permission_title)
            builder.setMessage(textId)
            builder.setPositiveButton(
                R.string.permission_button
            ) { _, _ ->
                PermissionHelper.makeRequest(context)
            }
        }
        if (layoutId != null) {

            builder.setView(layoutId)
            builder.setPositiveButton(context.getString(R.string.dialog_save_button_text)) { _, _ ->
                //call with  listener recordViewModel.saveRecordInDB()
                //attach input from edittext
            }
        }
        val dialog = builder.create()
        dialog.show()
    }
}
