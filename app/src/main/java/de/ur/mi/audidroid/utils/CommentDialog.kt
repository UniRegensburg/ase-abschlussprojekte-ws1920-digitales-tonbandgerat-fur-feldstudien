package de.ur.mi.audidroid.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.ExpandableMarkAndTimestamp
import de.ur.mi.audidroid.viewmodels.EditRecordingViewModel

object CommentDialog {

    private lateinit var dialog: androidx.appcompat.app.AlertDialog

    fun createDialog(
        context: Context,
        markTimestampToBeEdited: ExpandableMarkAndTimestamp? = null,
        layoutId: Int? = null,
        viewModel: EditRecordingViewModel? = null,
        errorMessage: String? = null
    ) {
        val builder = MaterialAlertDialogBuilder(context)
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val dialogView: View = inflater.inflate(layoutId!!, null)
        val editText: EditText = dialogView.findViewById(R.id.dialog_add_comment_edit_text)
        if (markTimestampToBeEdited != null) {
            editText.setText(markTimestampToBeEdited.markAndTimestamp.markTimestamp.markComment)
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
                if (markTimestampToBeEdited!!.markAndTimestamp.markTimestamp.markComment == null)
                    context.getString(R.string.add_comment_dialog_header)
                else
                    context.getString(R.string.edit_comment_dialog_header)
            )
            setPositiveButton(context.getString(R.string.dialog_save_button_text)) { _, _ ->
                var commentInput: String? = editText.text.toString()
                if (commentInput == "") commentInput = null
                viewModel?.onMarkTimestampUpdateClicked(
                    commentInput,
                    markTimestampToBeEdited
                )
                KeyboardHelper.hideSoftKeyboard(editText)
            }
            setNegativeButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
                KeyboardHelper.hideSoftKeyboard(editText)
                viewModel?.cancelCommentSaving()
            }
        }
        dialog = builder.create()
        dialog.show()
        dialog.setCancelable(true)
        dialog.setOnCancelListener {
            viewModel!!.cancelCommentSaving()
            dialog.findViewById<EditText>(R.id.dialog_add_comment_edit_text)?.let { editText ->
                KeyboardHelper.hideSoftKeyboard(editText)
            }
        }
        dialog.setOnDismissListener {
            dialog.findViewById<EditText>(R.id.dialog_add_comment_edit_text)?.let { editText ->
                KeyboardHelper.hideSoftKeyboard(editText)
            }
        }
    }
}
