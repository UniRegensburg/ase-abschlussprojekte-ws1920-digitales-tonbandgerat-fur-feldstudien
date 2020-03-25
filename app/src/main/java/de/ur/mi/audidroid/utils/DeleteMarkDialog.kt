package de.ur.mi.audidroid.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.MarkAndTimestamp
import de.ur.mi.audidroid.viewmodels.EditRecordingViewModel

object DeleteMarkDialog {

    private lateinit var dialog: androidx.appcompat.app.AlertDialog

    fun createDialog(
        context: Context,
        markToBeEdited: MarkAndTimestamp? = null,
        viewModel: EditRecordingViewModel? = null
    ) {
        val builder = MaterialAlertDialogBuilder(context)
        with(builder) {
            setTitle(context.getString(R.string.delete_mark_dialog_header))
            setPositiveButton(context.getString(R.string.delete)) { _, _ ->
                viewModel?.deleteMark(markToBeEdited!!.markTimestamp.mid)
            }
            setNegativeButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
                viewModel?.cancelDelete()
            }
        }
        dialog = builder.create()
        dialog.show()
        dialog.setCancelable(true)
    }
}
