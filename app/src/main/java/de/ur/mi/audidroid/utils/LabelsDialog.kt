package de.ur.mi.audidroid.utils

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
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
        if (type == R.string.alert_dialog) {
            val inflater: LayoutInflater = LayoutInflater.from(context)
            val dialogView: View = inflater.inflate(layoutId!!, null)
            val editText: EditText = dialogView.findViewById(R.id.dialog_add_label_edit_text)
            if (labelToBeEdited != null) {
                editText.setText(labelToBeEdited.labelName)
            }
            val pos: Int = editText.text.length
            editText.requestFocus()
            editText.setSelection(pos)
            KeyboardHelper.showSoftKeyboard(editText)
            builder.setView(dialogView)
            if (errorMessage != null) {
                builder.setMessage(errorMessage)
            }
            with(builder) {
                setTitle(
                    if (labelToBeEdited != null) String.format(
                        context.getString(R.string.rename_label_dialog_header),
                        labelToBeEdited.labelName
                    ) else context.getString(R.string.create_label_dialog_header)
                )
                setPositiveButton(context.getString(R.string.dialog_save_button_text)) { _, _ ->
                    var nameInput: String? = editText.text.toString()
                    if (nameInput == "") nameInput = null
                    if (labelToBeEdited != null) {
                        viewModel?.onLabelUpdateClicked(nameInput, labelToBeEdited)
                    } else {
                        viewModel?.onLabelSaveClicked(nameInput)
                    }
                    KeyboardHelper.hideSoftKeyboard(editText)
                }
                setNegativeButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
                    KeyboardHelper.hideSoftKeyboard(editText)
                    viewModel?.cancelSaving()
                }
            }
        }
        if (type == R.string.confirm_dialog) {
            if (errorMessage != null) {
                builder.setMessage(errorMessage)
            }
            with(builder) {
                setMessage(
                    String.format(
                        context.getString(R.string.delete_label_dialog_header),
                        labelToBeEdited!!.labelName
                    )
                )
                setPositiveButton(context.getString(R.string.delete)) { _, _ ->
                    viewModel?.deleteLabelFromDB(labelToBeEdited)
                }
                setNegativeButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
                    viewModel?.cancelSaving()
                }
            }
        }
        dialog = builder.create()
        dialog.show()
        dialog.setCancelable(true)
        dialog.setOnCancelListener {
            viewModel!!.cancelSaving()
            dialog.findViewById<EditText>(R.id.dialog_add_label_edit_text)?.let { editText ->
                KeyboardHelper.hideSoftKeyboard(editText)
            }
        }
        dialog.setOnDismissListener {
            dialog.findViewById<EditText>(R.id.dialog_add_label_edit_text)?.let { editText ->
                KeyboardHelper.hideSoftKeyboard(editText)
            }
        }
    }
}
