package de.ur.mi.audidroid.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.viewmodels.FilesViewModel
import de.ur.mi.audidroid.viewmodels.FolderViewModel

/**
 * FolderDialog manages all user input regarding the folders (except setting a new external one).
 * It's called upon at the creation of a new folder, it's deletion and the move of a recording.
 * @author: Lisa Sanladerer
 */
object FolderDialog {

    private lateinit var dialog: AlertDialog

    fun createDialog(
        context: Context,
        type: Int,
        folderViewModel: FolderViewModel,
        filesViewModel: FilesViewModel,
        layoutId: Int? = null,
        folderToBeEdited: FolderEntity? = null,
        errorMessage: String? = null,
        addFolder: Boolean? = null,
        recordingToBeMoved: EntryEntity? = null,
        listOfAvailableFolders: List<FolderEntity>? = null
        ) {
        val builder = AlertDialog.Builder(context)
        if (errorMessage != null) {
            builder.setMessage(errorMessage)
        }

        if (type == R.string.alert_dialog){
            //Dialog for the creation of a new internal folder.
            if (addFolder != null) {
                val inflater: LayoutInflater = LayoutInflater.from(context)
                val dialogView: View = inflater.inflate(layoutId!!, null)
                val editText: EditText = dialogView.findViewById(R.id.dialog_add_folder_edit_text)
                if (folderToBeEdited != null) {
                    editText.setText(folderToBeEdited.folderName)
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
                        String.format(
                            context.getString(R.string.create_folder_dialog_header)
                        )
                    )
                    setPositiveButton(context.getString(R.string.dialog_save_button_text)) { _, _ ->
                        val nameInput: String? = editText.text.toString()
                        folderViewModel.onFolderSaveClicked(nameInput!!, folderToBeEdited)
                        KeyboardHelper.hideSoftKeyboard(editText)
                    }
                    setNegativeButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
                        KeyboardHelper.hideSoftKeyboard(editText)
                        folderViewModel.cancelFolderDialog()
                    }
                }
            }

            //Dialog for the move of a recording to another folder.
            if (recordingToBeMoved != null){
                var position = -1
                val folderNameArray =
                    getFolderOptions(context, listOfAvailableFolders, recordingToBeMoved)
                if (folderNameArray.isNotEmpty()) {
                    builder.setTitle(R.string.move_file_dialog_header)
                    with(builder) {
                        builder.setSingleChoiceItems(folderNameArray, position) { _, which ->
                            position = which
                        }
                        setPositiveButton(R.string.popup_menu_option_move_file) { _, _ ->
                             if (position != -1){
                                 filesViewModel.recordingMoveValid(recordingToBeMoved, listOfAvailableFolders!![position].uid)
                                 folderViewModel.onMoveRecordingToFolder(recordingToBeMoved, listOfAvailableFolders[position])

                            }else{
                                filesViewModel.cancelFolderDialog()
                            }
                        }
                        setNeutralButton(R.string.popup_menu_cancel) { _, _ ->
                            filesViewModel.cancelFolderDialog()
                        }
                        setNegativeButton( R.string.dialog_no_folder) { _, _ ->
                            filesViewModel.recordingMoveValid(recordingToBeMoved,null)
                            folderViewModel.onMoveRecordingToFolder(recordingToBeMoved,null)

                        }
                    }
                } else {
                    with(builder) {
                        setTitle(R.string.no_folder_available)
                        setNegativeButton(R.string.permission_button) { _, _ ->
                            folderViewModel.cancelFolderDialog()
                        }
                    }
                }
            }
        }

        //Dialog for folder deletion.
        if (type == R.string.confirm_dialog){
            if (errorMessage != null){
                builder.setMessage(errorMessage)
            }
            with(builder){
                setMessage(
                    String.format(
                        context.getString(R.string.delete_folder_dialog_header),
                        folderToBeEdited!!.folderName
                    )
                )
                setPositiveButton(context.getString(R.string.delete)){ _, _ ->

                    if (!folderToBeEdited.isExternal){
                        val folderReferences = folderViewModel.onDeleteFolderAndContent(folderToBeEdited)
                        filesViewModel.deleteEntriesInFolders(folderReferences)
                    }else{
                        val folderReference = listOf(folderToBeEdited.uid)
                        filesViewModel.deleteEntriesInFolders(folderReference)
                        folderViewModel.onDeleteExternalFolder(folderToBeEdited)
                    }

                    folderViewModel.cancelFolderDialog()


                }
                setNegativeButton(context.getString(R.string.dialog_cancel_button_text)){_, _ ->
                    folderViewModel.cancelFolderDialog()
                }
            }
        }

        dialog = builder.create()
        dialog.show()

        disableRemoveFromFolder(type, recordingToBeMoved)

        dialog.setCancelable(true)
        dialog.setOnDismissListener {
            dialog.findViewById<EditText>(R.id.dialog_add_label_edit_text)?.let { editText ->
                KeyboardHelper.hideSoftKeyboard(editText)
            }
        }
    }

    private fun disableRemoveFromFolder(type: Int, recordingToBeMoved: EntryEntity?){
        val removeFromFolderBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        if (type == R.string.alert_dialog && recordingToBeMoved != null){
            if (recordingToBeMoved.folder == null){
                removeFromFolderBtn.isEnabled = false
            }
        }
    }

   // Gets the possible options for a file move. Once a file is stored outside the app, it won't be able to become
   // internalized again.
    private fun getFolderOptions(context: Context, listOfAvailableFolders: List<FolderEntity>?, entryToBeMoved: EntryEntity): Array<String>{
        val folderNameList: ArrayList<String> = ArrayList()
        if (entryToBeMoved.recordingPath.startsWith(context.getString(R.string.content_uri_prefix))){
            listOfAvailableFolders!!.forEach {
                if (it.isExternal){
                    folderNameList.add(it.folderName)
                }
            }
        }else{
            listOfAvailableFolders!!.forEach {
                folderNameList.add(it.folderName)
            }
        }
        val folderNameArray = arrayOfNulls<String>(folderNameList.size)
        return folderNameList.toArray(folderNameArray)
    }
}
