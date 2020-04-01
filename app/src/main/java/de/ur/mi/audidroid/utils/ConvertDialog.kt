package de.ur.mi.audidroid.utils

import android.content.Context
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.viewmodels.FilesViewModel

object ConvertDialog {

    private lateinit var dialog: androidx.appcompat.app.AlertDialog

    fun createDialog(
        context: Context,
        layoutId: Int? = null,
        viewModel: FilesViewModel
    ) {
        val builder = MaterialAlertDialogBuilder(context)
        with(builder) {
            setView(layoutId!!)
            setTitle(context.getString(R.string.choose_file_format))
            setPositiveButton(context.getString(R.string.dialog_export_button_text)) { _, _ ->
                val selectedButtonId: Int = dialog.findViewById<RadioGroup>(R.id.dialog_convert_radio_group)!!.checkedRadioButtonId
                val convertFormat: String = dialog.findViewById<RadioButton>(selectedButtonId)!!.text.toString()
                viewModel.shareRecording(convertFormat)
            }
            setNegativeButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
                viewModel.cancelExporting()
            }
        }
        dialog = builder.create()
        dialog.show()
        dialog.setCancelable(true)
        dialog.setOnCancelListener {
            viewModel.cancelExporting()
        }
    }
}
