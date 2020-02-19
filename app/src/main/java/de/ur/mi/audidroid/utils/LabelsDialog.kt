package de.ur.mi.audidroid.utils

import android.content.Context
import android.widget.EditText
import androidx.core.view.LayoutInflaterCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.LabelEntity
import de.ur.mi.audidroid.viewmodels.EditLabelsViewModel

object LabelsDialog {

    private lateinit var dialog: androidx.appcompat.app.AlertDialog

    fun createDialog(
        context: Context,
        type: Int,
        labelToBeEdited: LabelEntity? = null,
        layoutId: Int? = null,
        viewModel: EditLabelsViewModel? = null,
        errorMessage: String? = null
    ) {
        val builder = MaterialAlertDialogBuilder(context)
        if(type == R.string.alert_dialog) {
            builder.setView(layoutId!!)
            if(errorMessage != null) {
                builder.setMessage(errorMessage)
            }
            with(builder) {
                setTitle(if(labelToBeEdited != null) context.getString(R.string.rename_label_dialog_header) else context.getString(R.string.create_label_dialog_header))
                setPositiveButton(context.getString(R.string.dialog_save_button_text)) { _, _ ->
                    var nameInput: String? = dialog.findViewById<EditText>(R.id.dialog_add_label_edit_text)!!.text.toString()
                    if(nameInput == "") nameInput = null
                    if(labelToBeEdited != null) {
                        viewModel?.onLabelUpdateClicked(nameInput, labelToBeEdited)
                    } else {
                        viewModel?.onLabelSaveClicked(nameInput)
                    }
                }
                setNegativeButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
                    viewModel?.cancelSaving()
                }
            }
        }
        if (type == R.string.confirm_dialog) {
            if(errorMessage != null) {
                builder.setMessage(errorMessage)
            }
            with(builder) {
                setMessage(context.getString(R.string.delete_label_dialog_header))
                setPositiveButton(context.getString(R.string.delete)) { _, _ ->
                    viewModel?.deleteLabelFromDB(labelToBeEdited!!)
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