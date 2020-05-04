package de.ur.mi.audidroid.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.viewmodels.FilesViewModel

object FolderDialog{

    private lateinit var editText: EditText

    fun createDialog(context: Context, layoutId: Int, viewModel: FilesViewModel, errorMessage: String? = null, folderToBeEdited: FolderEntity?, deleteFolder: Boolean){
        val builder = MaterialAlertDialogBuilder(context)
        if (errorMessage != null) {
            builder.setMessage(errorMessage)
        }
        if(!deleteFolder){
            val inflater: LayoutInflater = LayoutInflater.from(context)
            val dialogView: View = inflater.inflate(layoutId, null)
            builder.setView(dialogView)
            editText = dialogView.findViewById<EditText>(R.id.dialog_create_folder_name)!!
            with(builder) {
                setTitle(
                    if (folderToBeEdited != null) String.format(
                        context.getString(R.string.folder_rename_title),
                        folderToBeEdited.folderName
                    ) else context.getString(R.string.folder_dialog_title)
                )
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
                    viewModel.resetValues()
                }

            }
        }
        else{
            with(builder) {
                setTitle(
                    if (folderToBeEdited != null) String.format(
                        context.getString(R.string.folder_delete_title),
                        folderToBeEdited.folderName
                    ) else context.getString(R.string.folder_delete_folder)
                )
                setMessage(context.getString(R.string.folder_delete_message))
                setPositiveButton(context.getString(R.string.folder_dialog_ok)) { _, _ ->
                    viewModel.deleteFolder(folderToBeEdited!!)
                }
                setNegativeButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
                    viewModel.resetValues()
                }
            }
        }
        val dialog = builder.create()
        dialog.setOnCancelListener{
            viewModel.resetValues()
        }
        dialog.show()
    }
}
