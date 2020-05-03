package de.ur.mi.audidroid.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.ur.mi.audidroid.viewmodels.FilesViewModel
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.FolderEntity

object CreateFolderDialog{

    private lateinit var editText: EditText

    fun createDialog(context: Context, viewModel: FilesViewModel, errorMessage: String? = null, folderToBeEdited: FolderEntity?,  layoutId: Int){
        val builder = MaterialAlertDialogBuilder(context)
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val dialogView: View = inflater.inflate(layoutId, null)

        if (errorMessage != null) {
            builder.setMessage(errorMessage)
        }
        with(builder) {
            setTitle(
                if (folderToBeEdited != null) String.format(
                    context.getString(R.string.folder_rename_pre_fill),
                    folderToBeEdited.folderName
                ) else context.getString(R.string.folder_dialog_title)
            )
            editText = dialogView.findViewById<EditText>(R.id.dialog_create_folder_name)!!
            if(folderToBeEdited!=null){
                editText.setText(folderToBeEdited.folderName)
                editText.setSelection(editText.text.length)
            }
            editText.showKeyboard()
            setPositiveButton(context.getString(R.string.folder_dialog_ok)) { _, _ ->
                if(folderToBeEdited != null) viewModel.renameFolder(folderToBeEdited, editText.text.toString())
                else  viewModel.createFolder(editText.text.toString())
            }
            setNegativeButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
            }

        }
        val dialog = builder.create()
        dialog.setCancelable(true)
        dialog.show()
    }
}
