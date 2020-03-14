package de.ur.mi.audidroid.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.viewmodels.RecordViewModel

object QuitRecordingDialog {

    fun createDialog(context: Context, text: String, recordViewModel: RecordViewModel){
        val builder = MaterialAlertDialogBuilder(context)
        with(builder) {
            setMessage(text)
            setPositiveButton(
                R.string.delete
            ) { _, _ ->
                recordViewModel.deleteRecord()
            }
            setNegativeButton(R.string.dialog_cancel_button_text){_, _ ->
                recordViewModel.cancelDialog()
            }
        }
        val dialog: androidx.appcompat.app.AlertDialog = builder.create()
        dialog.show()
    }
}
