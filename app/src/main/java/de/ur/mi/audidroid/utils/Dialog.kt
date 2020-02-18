package de.ur.mi.audidroid.utils

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.viewmodels.RecordViewModel


/**
 * The Dialog object creates a dialog depending on the parameters.
 * @author: Sabine Roth
 */

object Dialog {

    private lateinit var dialog: AlertDialog

    fun createDialog(
        context: Context,
        layoutId: Int? = null,
        textId: Int? = null,
        viewModel: RecordViewModel? = null,
        errorMessage: String? = null
    ) {
        val builder = AlertDialog.Builder(context)
        if (layoutId != null) {
            builder.setView(layoutId)
            if (errorMessage != null) {
                builder.setMessage(errorMessage)
            }
            with(builder) {
                setPositiveButton(context.getString(R.string.dialog_save_button_text)) { _, _ ->
                    var nameInput: String? =
                        dialog.findViewById<EditText>(R.id.dialog_save_recording_edittext_name)
                            .text.toString()
                    if (nameInput == "") nameInput = null
                    //TODO: Change path parameter to user input in issue #12
                    viewModel?.getNewFileFromUserInput(nameInput, null)
                }
                setNeutralButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
                    viewModel?.cancelSaving()
                }
            }
        }
        if (textId != null) {
            with(builder) {
                setTitle(R.string.permission_title)
                setMessage(textId)
                setPositiveButton(
                    R.string.permission_button
                ) { _, _ ->
                    PermissionHelper.makeRequest(context)
                }
            }
        }
        dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
    }
}
