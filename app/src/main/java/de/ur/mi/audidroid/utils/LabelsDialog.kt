package de.ur.mi.audidroid.utils

import android.content.Context
import android.widget.EditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.viewmodels.EditLabelsViewModel

object LabelsDialog {

    private lateinit var dialog: androidx.appcompat.app.AlertDialog

    fun createDialog(
        context: Context,
        layoutId: Int? = null,
        viewModel: EditLabelsViewModel? = null,
        errorMessage: String? = null
    ) {
        val builder = MaterialAlertDialogBuilder(context)
        if (layoutId != null) {
            builder.setView(layoutId)
            if (errorMessage != null) {
                builder.setMessage(errorMessage)
            }
            with(builder) {
                setTitle(context.getString(R.string.create_label_dialog_header))
                setPositiveButton(context.getString(R.string.dialog_save_button_text)) { _, _ ->
                    var nameInput: String? =
                        dialog.findViewById<EditText>(R.id.dialog_add_label_edit_text)!!
                            .text.toString()
                    if (nameInput == "") nameInput = null
                    viewModel?.onLabelSaveClicked(nameInput)
                }
                setNegativeButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
                    viewModel?.cancelSaving()
                }
            }
        }
        dialog = builder.create()
        dialog.setCancelable(true)
        dialog.setOnCancelListener {
            viewModel!!.cancelSaving()
        }
        dialog.show()
    }
}