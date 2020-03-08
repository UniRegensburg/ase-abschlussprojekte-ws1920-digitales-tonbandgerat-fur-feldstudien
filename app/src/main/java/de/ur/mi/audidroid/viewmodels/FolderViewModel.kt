package de.ur.mi.audidroid.viewmodels


import android.app.Application
import android.content.res.Resources
import android.net.Uri
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.StorageHelper
import java.util.regex.Pattern

class FolderViewModel(dataSource: Repository, application: Application) :
    AndroidViewModel(application) {

    private val repository = dataSource
    private val context = getApplication<Application>().applicationContext
    private lateinit var frameLayout: FrameLayout
    val res: Resources = context.resources
    private val _createAlertDialog = MutableLiveData<Boolean>()
    private val _createConfirmDialog = MutableLiveData<Boolean>()
    val allFolders: LiveData<List<FolderEntity>> = repository.getAllFolders()
    var allInternalFolders: LiveData<List<FolderEntity>> = repository.getInternalFolders()
    var allExternalFolders: LiveData<List<FolderEntity>> = repository.getExternalFolders()
    var allInternalFoldersSorted = MediatorLiveData<List<FolderEntity>>()
    var allExternalFoldersSorted = MediatorLiveData<List<FolderEntity>>()
    var folderToBeCreated: Boolean? = null
    var dialogType: Int = R.string.confirm_dialog
    var errorMessage: String? = null
    var folderToBeEdited: FolderEntity? = null


    private var _showSnackbarEvent = MutableLiveData<Boolean>()

    val showSnackbarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = null
    }

    fun cancelFolderDialog(){
        errorMessage = null
        _createAlertDialog.value = false
        folderToBeCreated = null
    }

    fun isSubfolder(folder: FolderEntity): Boolean{
        folder.parentDir?.let { return true }
        return false
    }


    fun initFolderSorting(){
        println("______________________________")
        if (allInternalFoldersSorted.value == null){
            allInternalFoldersSorted.addSource(allInternalFolders){
                println(allFolders)
                println(allInternalFolders.value)
                allInternalFoldersSorted.value = StorageHelper.getInternalFolderHierarchy(allInternalFolders.value!!)
                println(allInternalFolders.value)
            }

        }
        if (allExternalFoldersSorted.value == null){
            allExternalFoldersSorted.addSource(allExternalFolders){
                println(allExternalFolders.value)
                allExternalFoldersSorted.value = allExternalFolders.value

            }
        }
    }

    val createAlertDialog: MutableLiveData<Boolean>
        get() = _createAlertDialog

    val createConfirmDialog: MutableLiveData<Boolean>
        get() = _createConfirmDialog

    //queries for recordings, which are not assigned to any folders
    fun getRecordingsWithNoFolder(): LiveData<List<EntryEntity>>{
        return repository.getRecordingByFolder(null)
    }

    fun getAllRecordingsByFolder(folder : FolderEntity): LiveData<List<EntryEntity>>{
        return repository.getRecordingByFolder(folder.uid)
    }


    fun onFolderMenuClicked(folder: FolderEntity, view:View){
        val popupMenu = PopupMenu(context, view)
        folderToBeEdited = folder
        popupMenu.menuInflater.inflate(R.menu.popup_menu_folder, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId){
                R.id.action_add_subfolder ->
                    onAddFolderClicked(folder)
                R.id.action_delete_folder ->
                    onDeleteFolderClicked(folder)
            }
            true
        }
        popupMenu.show()
    }

    //handels the deletion of folder
    fun onDeleteFolderClicked( folderEntity: FolderEntity){
        _createConfirmDialog.value = true
        dialogType = R.string.confirm_dialog
        folderToBeEdited = folderEntity
    }

    //creates a reference in Database for folder
    fun handleActivityResult(result: String){
        println(allFolders.value!!)
        val existingFolder = StorageHelper.checkExternalFolderReference(allFolders.value!!,result)
        if (existingFolder == null) {
            StorageHelper.createFolderFromUri(repository, result)
        }
    }

    fun deleteFolderFromDB(folderList: MutableList<FolderEntity>) {
        folderList.forEach { repository.deleteFolder(it) }
        folderToBeEdited = null
    }

    //handels the creation of a new folder
    //passes call of 'add basic internal folder' button on
    fun addInternalFolder(){
        onAddFolderClicked(null)
    }

    fun onAddFolderClicked(folder: FolderEntity? = null){
        _createAlertDialog.value = true
        dialogType = R.string.alert_dialog
        folderToBeCreated = true
        folderToBeEdited = folder
    }

    fun onFolderSaveClicked(nameInput: String, parentFolder: FolderEntity?){
        _createAlertDialog.value = false
        if (!validName(nameInput)) {
            errorMessage = res.getString(R.string.dialog_label_invalid_name)
            _createAlertDialog.value = true
            return
        }
        createFolderInDB(nameInput, parentFolder)
    }

    private fun createFolderInDB(nameInput: String, parentFolder: FolderEntity?){
        var nestingDescr = ""
        var parentFolderRef: Int? = null
        if (parentFolder != null){
            nestingDescr = parentFolder.folderName
            parentFolderRef = parentFolder.uid
        }
        val newFolderEntity = FolderEntity(0,nameInput,"path",
            false, parentFolderRef , nestingDescr)
        repository.insertFolder(newFolderEntity)
        folderToBeEdited = null
    }

    //handels the move of a folder from one folder to another
    fun onEntryMoveFolderClicked(entryEntity: EntryEntity, folderUid: Int?, folderPath: String?){
        if (folderPath!!.contains(res.getString(R.string.content_uri_prefix))||
                entryEntity.recordingPath.contains(res.getString(R.string.content_uri_prefix))){
            StorageHelper.moveEntryStorage(context, entryEntity, folderPath)
        }
        updateEntryFolderInDB(entryEntity,folderUid)
    }

    fun updateEntryFolderInDB(entryEntity: EntryEntity, folderUid: Int?){
        val updatedEntry = EntryEntity(entryEntity.uid,entryEntity.recordingName,entryEntity.recordingPath,
            entryEntity.date, entryEntity.duration, folderUid, entryEntity.labels)
        repository.updateEntry(updatedEntry)
    }

    /*
    //handels the sorting of the folders, so they can be displayed in order
    fun getAllInternalSubFolders(folderList: MutableList<FolderEntity>): MutableList<FolderEntity>{
        val foldersToBeDeleted = folderList
        foldersToBeDeleted.forEach { parent ->
            allFolders.value!!.forEach {
                if (!foldersToBeDeleted.contains(it)){
                    if (parent.uid == it.parentDir){
                        foldersToBeDeleted.add(it)
                        return getAllInternalSubFolders(foldersToBeDeleted)
                    }
                }
            }
        }
        return foldersToBeDeleted
    }*/

    private fun validName(name: String?): Boolean {
        val folderName = name ?: ""
        return Pattern.compile("^[a-zA-Z0-9_-]{1,10}$").matcher(folderName).matches()
    }

    fun initializeLayout(layout: FrameLayout) {
        frameLayout = layout
    }

    private fun showSnackBar(text: String) {
        Snackbar.make(frameLayout, text, Snackbar.LENGTH_LONG).show()
    }
}
