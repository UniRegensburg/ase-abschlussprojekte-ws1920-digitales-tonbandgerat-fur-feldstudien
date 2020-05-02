package de.ur.mi.audidroid.utils

import android.content.Context
import android.widget.EditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.ur.mi.audidroid.viewmodels.FilesViewModel
import de.ur.mi.audidroid.R

object CreateFolderDialog{

    private lateinit var dialog: androidx.appcompat.app.AlertDialog
    private lateinit var editText: EditText

    fun createDialog(context: Context, viewModel: FilesViewModel, errorMessage: String? = null){
        val builder = MaterialAlertDialogBuilder(context)
        if (errorMessage != null) {
            builder.setMessage(errorMessage)
        }
        with(builder) {
            setTitle(
                context.getString(R.string.folder_dialog_title)
            )
            setView(R.layout.create_folder_dialog)
            setPositiveButton(context.getString(R.string.folder_dialog_ok)) { _, _ ->
                viewModel.createFolder(editText.text.toString())
            }
            setNegativeButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
            }

        }
        dialog = builder.create()
        dialog.setCancelable(true)
        dialog.show()
        editText = dialog.findViewById<EditText>(R.id.dialog_create_folder_name)!!
        editText.showKeyboard()
    }
}
