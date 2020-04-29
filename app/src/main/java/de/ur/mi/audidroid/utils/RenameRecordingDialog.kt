package de.ur.mi.audidroid.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.viewmodels.FilesViewModel
import kotlinx.android.synthetic.main.name_recording_dialog.*

object RenameRecordingDialog {
    private lateinit var dialog: androidx.appcompat.app.AlertDialog

    fun createDialog(
        context: Context,
        viewModel: FilesViewModel,
        layoutId: Int,
        recording: RecordingAndLabels,
        errorMessage: String?

    ) {
        val builder = MaterialAlertDialogBuilder(context)
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val dialogView: View = inflater.inflate(layoutId, null)
        val editText: EditText = dialogView.findViewById(R.id.dialog_name_recording_edit_text)
        editText.setText(recording.recordingName)
        val pos: Int = editText.text.length
        editText.showKeyboard()
        editText.setSelection(pos)
        errorMessage?.let { builder.setMessage(errorMessage) }
        builder.setView(dialogView)
        with(builder) {
            setTitle(context.getString(R.string.dialog_rename_title))
            setPositiveButton(context.getString(R.string.dialog_rename)) { _, _ ->
                var nameInput: String? = editText.text.toString()

                viewModel.renameRecording(recording, nameInput)
                editText.hideKeyboard()
            }
            setNegativeButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
                viewModel.cancelNamingDialog()
                editText.hideKeyboard()
            }
        }
        dialog = builder.create()
        dialog.show()
        dialog.setOnCancelListener {
            viewModel.cancelNamingDialog()
            dialog.dialog_name_recording_edit_text.hideKeyboard()
        }
        dialog.setOnDismissListener {
            viewModel.cancelNamingDialog()
            dialog.dialog_name_recording_edit_text.hideKeyboard()
        }
    }
}