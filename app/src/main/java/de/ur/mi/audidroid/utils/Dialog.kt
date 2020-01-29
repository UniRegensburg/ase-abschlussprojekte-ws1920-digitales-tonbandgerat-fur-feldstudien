package de.ur.mi.audidroid.utils

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.widget.EditText
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.viewmodels.RecordViewModel


/**
 * The Dialog object creates a dialog depending on the parameters.
 * @author: Sabine Roth
 */

//TODO: use listener instead of RecorderViewModel
object Dialog {

    private lateinit var dialog: AlertDialog

    fun createDialog(context: Context, layoutId: Int? = null, textId: Int? = null) {
        val builder = AlertDialog.Builder(context)
        if (layoutId != null) {
            builder.setView(layoutId)
            builder.setPositiveButton(context.getString(R.string.dialog_save_button_text)) { _, _ ->
                val nameInput =
                    dialog.findViewById<EditText>(R.id.dialog_save_recording_edittext_name)
                        .text.toString()
                val pathInput =
                    dialog.findViewById<EditText>(R.id.dialog_save_recording_edittext_path)
                        .text.toString()
                val application = Application()
                val vm = RecordViewModel(Repository(context as Application), application, context)
                vm.saveRecordInDB(nameInput, pathInput)
            }
        }
        if (textId != null) {
            builder.setTitle(R.string.permission_title)
            builder.setMessage(textId)
            builder.setPositiveButton(
                R.string.permission_button
            ) { _, _ ->
                PermissionHelper.makeRequest(context)
            }
        }
        dialog = builder.create()
        dialog.show()
    }

}
