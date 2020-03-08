package de.ur.mi.audidroid.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.viewmodels.FilesViewModel
import de.ur.mi.audidroid.viewmodels.FolderViewModel

object FolderDialog {
    private lateinit var dialog: AlertDialog

    fun createDialog(
        context: Context,
        type: Int,
        folderToBeEdited: FolderEntity? = null,
        layoutId: Int? = null,
        viewModel: FolderViewModel? = null,
        filesViewModel: FilesViewModel? = null,
        errorMessage: String? = null,
        folderToBeCreated: Boolean? = null,
        entryToBeMoved: EntryEntity? = null,
        listOfAvailableFolders: List<FolderEntity>? = null
    ){
        val builder = AlertDialog.Builder(context)

        if (type == R.string.alert_dialog){
            if (entryToBeMoved == null) {
                //dialog for creating a new folder
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
                        if (folderToBeCreated == true) String.format(
                            context.getString(R.string.create_folder_dialog_header),
                            R.string.popup_menu_option_add_folder
                        ) else context.getString(R.string.create_folder_dialog_header)
                    )
                    setPositiveButton(context.getString(R.string.dialog_save_button_text)) { _, _ ->
                        onCreateFolder(folderToBeEdited,viewModel!!,editText)
                    }
                    setNegativeButton(context.getString(R.string.dialog_cancel_button_text)) { _, _ ->
                        KeyboardHelper.hideSoftKeyboard(editText)
                        viewModel?.cancelFolderDialog()
                    }
                }
            }else{
                //dialog for recording move to folder
                var position = -1
                if(errorMessage != null){
                    builder.setMessage(errorMessage)
                }

                val folderNameArray = getFolderOptions(context, listOfAvailableFolders, entryToBeMoved)
                if (listOfAvailableFolders!!.isNotEmpty()){
                    with(builder){
                        setTitle(R.string.move_file_dialog_header)
                        builder.setSingleChoiceItems(folderNameArray, position){ dialog, which ->
                            position = which
                        }
                        setPositiveButton(R.string.move_file){dialog, which ->
                            onMoveFolder(entryToBeMoved,listOfAvailableFolders,viewModel!!,position)
                        }
                        setNegativeButton(R.string.cancel_button){_, _ ->
                            viewModel?.cancelFolderDialog()
                        }
                        setNeutralButton(R.string.dialog_no_folder){_, _ ->
                            viewModel!!.onEntryMoveFolderClicked(entryToBeMoved,null, null)
                        }
                    }
                }else{
                    with(builder){
                        setTitle(R.string.no_folder_available)
                        setNegativeButton(R.string.permission_button){_, _ ->
                            viewModel?.cancelFolderDialog()
                        }
                    }
                }

            }
        }
        //dialog for folder deletion
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
                    if (folderToBeEdited != null){
                        onDeleteFolderAndContent(listOfAvailableFolders!! ,folderToBeEdited, viewModel!!, filesViewModel!!)
                    }
                }
                setNegativeButton(context.getString(R.string.dialog_cancel_button_text)){_, _ ->
                    viewModel?.cancelFolderDialog()
                }
            }
        }
        dialog = builder.create()
        dialog.show()
        dialog.setCancelable(true)
        dialog.setOnDismissListener {
            dialog.findViewById<EditText>(R.id.dialog_add_label_edit_text)?.let { editText ->
                KeyboardHelper.hideSoftKeyboard(editText)
            }
        }
    }

    private fun onCreateFolder(folderToBeEdited: FolderEntity?, viewModel: FolderViewModel, editText: EditText){
        var nameInput: String? = editText.text.toString()
        if (nameInput == "") nameInput = null
        viewModel.onFolderSaveClicked(nameInput!!, folderToBeEdited)
        KeyboardHelper.hideSoftKeyboard(editText)
    }

    //moves the recording between folders
    private fun onMoveFolder(entryToBeMoved: EntryEntity, listOfAvailableFolders: List<FolderEntity>?,
                             viewModel: FolderViewModel, position: Int){
        if (position != -1){
            viewModel.onEntryMoveFolderClicked(entryToBeMoved, listOfAvailableFolders!![position].uid,
                listOfAvailableFolders[position].dirPath)
        }else{
            viewModel.cancelFolderDialog()
        }
    }

    //deletes folder and subfolders along with their content
    private fun onDeleteFolderAndContent(allFolders: List<FolderEntity>, folderToBeEdited: FolderEntity,viewModel: FolderViewModel,
                                         filesViewModel: FilesViewModel){
        if (folderToBeEdited.isExternal == false){
            //internal
            val folderAndSubfolders = mutableListOf(folderToBeEdited)
            StorageHelper.getAllInternalSubFolders(allFolders, folderAndSubfolders)
            filesViewModel.folderList = MutableLiveData(folderAndSubfolders)
            filesViewModel.deleteEntriesInInternalFolders(folderAndSubfolders)
            viewModel.deleteFolderFromDB(folderAndSubfolders)
        }else{
            println("JO, Folder is External")
        }

    }

    //gets folder names for choosable options
    private fun getFolderOptions(context: Context, listOfAvailableFolders: List<FolderEntity>?, entryToBeMoved: EntryEntity): Array<String>{

        var folderNameList: ArrayList<String> = ArrayList()
        println(entryToBeMoved.recordingPath)
        if (entryToBeMoved.recordingPath.startsWith(context.getString(R.string.content_uri_prefix))){
            listOfAvailableFolders!!.forEach {
                println(it)
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
    /*
    *

        var folderNameList: ArrayList<String> = ArrayList()
        listOfAvailableFolders!!.forEach {
            folderNameList.add(it.folderName)
        }
        val folderNameArray = arrayOfNulls<String>(folderNameList.size)
        return folderNameList.toArray(folderNameArray)
    * */


}
