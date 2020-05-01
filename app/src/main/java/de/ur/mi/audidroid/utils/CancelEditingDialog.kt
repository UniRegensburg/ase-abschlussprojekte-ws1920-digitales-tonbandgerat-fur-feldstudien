package de.ur.mi.audidroid.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.viewmodels.EditRecordingViewModel

object CancelEditingDialog {

    fun createDialog(context: Context, viewModel: EditRecordingViewModel) {
        val builder = MaterialAlertDialogBuilder(context)
        with(builder) {
            setMessage(context.getString(R.string.cancel_editing))
            setPositiveButton(
                R.string.delete
            ) { _, _ ->
                viewModel.deleteEditedRecording()
            }
            setNegativeButton(R.string.dialog_cancel_button_text) { _, _ ->
                viewModel.cancelDialog()
            }
        }
        val dialog: androidx.appcompat.app.AlertDialog = builder.create()
        dialog.setOnCancelListener {
            viewModel.cancelDialog()
        }
        dialog.show()
    }
}
