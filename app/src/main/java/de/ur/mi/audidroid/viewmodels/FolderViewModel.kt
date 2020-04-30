package de.ur.mi.audidroid.viewmodels


import android.app.Application
import android.content.res.Resources
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.StorageHelper
import java.util.regex.Pattern

class FolderViewModel(dataSource: Repository, application: Application) :
    AndroidViewModel(application) {

    private val repository = dataSource
    private val context = getApplication<Application>().applicationContext
    private val res: Resources = context.resources
    private val _createAlertFolderDialog = MutableLiveData<Boolean>()
    private val _createConfirmDialog = MutableLiveData<Boolean>()
    val allFolders: LiveData<List<FolderEntity>> = repository.getAllFolders()
    val allFoldersSorted = MediatorLiveData<List<FolderEntity>>()
    val externalFolderLiveData: LiveData<Int> = repository.getExternalFolderCount()
    var externalFolderCount = 0
    private var dialogType: Int = R.string.confirm_dialog
    var errorMessage: String? = null
    var addFolder: Boolean? = null
    var folderToBeEdited: FolderEntity? = null

    private var folderView: View? = null

    private var _showSnackbarEvent = MutableLiveData<String>()

    val createAlertFolderDialog: MutableLiveData<Boolean>
        get() = _createAlertFolderDialog

    val createConfirmDialog: MutableLiveData<Boolean>
        get() = _createConfirmDialog

    val showSnackbarEvent: LiveData<String>
        get() = _showSnackbarEvent

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = ""
    }

    fun cancelFolderDialog(){
        errorMessage = null
        _createAlertFolderDialog.value = false
        _createConfirmDialog.value = false
        folderView = null
    }

    fun isSubfolder(folderRef: Int?): Boolean{
        folderRef?.let { return true }
        return false
    }

    private fun getFolderExternal(folders: List<FolderEntity>, isExternal: Boolean): List<FolderEntity>?{
        val folderList = mutableListOf<FolderEntity>()
        folders.forEach {
            if (it.isExternal == isExternal){ folderList.add(it)}
        }
        return folderList
    }

    fun sortAllFolders() {
        allFoldersSorted.removeSource(allFolders)
        allFoldersSorted.addSource(allFolders){
            val sortedFolders = mutableListOf<FolderEntity>()
            val internalFolders = getFolderExternal(allFolders.value!!, false)
            val externalFolders = getFolderExternal(allFolders.value!!, true)
            val internalFoldersSorted = StorageHelper.getInternalFolderHierarchy(internalFolders)
            if (internalFolders!!.isNotEmpty()){sortedFolders.addAll(internalFoldersSorted!!.asIterable())}
            if (externalFolders!!.isNotEmpty()){sortedFolders.addAll(externalFolders.asIterable())}
            if (sortedFolders.isEmpty()){
               allFoldersSorted.value = null
            }else{
                allFoldersSorted.value = sortedFolders
            }
        }
    }

    fun onDeleteFolderClicked( folderEntity: FolderEntity, view: View){
        folderToBeEdited = folderEntity
        dialogType = R.string.confirm_dialog
        _createConfirmDialog.value = true
        folderView = view
    }

    // Handles the reference in the Database for the ActivityForResult result.
    fun handleActivityResult(result: String){
        val folders = allFolders.value
        val allFolderPaths: ArrayList<String> = arrayListOf()
        folders!!.forEach {
            if(it.dirPath != null){
                allFolderPaths.add(it.dirPath) }
            }
        val existingFolder = StorageHelper.checkExternalFolderReference(allFolderPaths, result)
        if (existingFolder == null) {
            StorageHelper.createFolderFromUri(repository, result)
        }
    }

    private fun deleteFolderFromDB(folderList: List<FolderEntity>) {
        folderList.forEach { repository.deleteFolder(it) }
        _showSnackbarEvent.value = res.getString(R.string.folder_deleted)
        folderToBeEdited = null
    }

    fun onDeleteFolderAndContent(folder: FolderEntity): List<Int>{
        val folderReferences = mutableListOf<Int>()
        if (!folder.isExternal){
           val allFoldersToBeDeleted = StorageHelper.getAllInternalSubFolders(allFolders.value!!, mutableListOf(folder))
            allFoldersToBeDeleted.forEach {
                folderReferences.add(it.uid) }
            deleteFolderFromDB(allFoldersToBeDeleted)
        }
        folderView!!.invalidate()
        folderView = null
        folderToBeEdited = null
        return folderReferences
    }

    fun onDeleteExternalFolder(folder: FolderEntity){
        StorageHelper.handleExternalFolderDeletion(context, folder.dirPath!!)
        deleteFolderFromDB(listOf(folder))
        folderView!!.invalidate()
        folderView = null
        folderToBeEdited = null
    }

    // Allows Databinding for the primary Add-Folder-Button
    fun onAddInternalFolderClicked(){
        onAddFolderClicked(null)
    }

    fun onAddFolderClicked(folder: FolderEntity? = null){
        addFolder = true
        folderToBeEdited = folder
        dialogType = R.string.alert_dialog
        _createAlertFolderDialog.value = true
    }

    fun onFolderSaveClicked(nameInput: String, parentFolder: FolderEntity?){
        _createAlertFolderDialog.value = false
        if(nameInput == ""){
            errorMessage = res.getString(R.string.dialog_missing_folder_name)
            _createAlertFolderDialog.value = true
            return
        }
        if (!validName(nameInput)) {
            errorMessage = res.getString(R.string.dialog_folder_invalid_name)
            _createAlertFolderDialog.value = true
            return
        }
        if (!folderNameUnique(nameInput)){
            errorMessage = res.getString(R.string.dialog_folder_name_occurance)
            _createAlertFolderDialog.value = true
            return
        }

        folderToBeEdited = null
        addFolder = null
        errorMessage = null
        createFolderInDB(nameInput, parentFolder)
        _showSnackbarEvent.value = res.getString(R.string.folder_created)
    }

    private fun createFolderInDB(nameInput: String, parentFolder: FolderEntity?){
        repository.insertFolder(StorageHelper.createInternalFolderEntity(nameInput,parentFolder))
    }

    private fun folderNameUnique(name: String?): Boolean{
        allFolders.value!!.forEach {
            if( it.folderName == name){
                return false
            }
        }
        return true
    }

    private fun validName(name: String?): Boolean {
        val folderName = name ?: ""
        return Pattern.compile("^[a-zA-Z0-9_-]{1,10}$").matcher(folderName).matches()
    }

    fun onMoveRecordingToFolder(recording: RecordingAndLabels, destFolder: FolderEntity?){
        var newRecordingPath: String? = null
        var folderRef: Int? = null
        var moveSuccessful = true

        if (destFolder != null) {
            folderRef = destFolder.uid
            if (destFolder.isExternal) {
                newRecordingPath =
                    StorageHelper.moveRecordingExternally(context, recording, destFolder.dirPath!!)
                if (newRecordingPath == null) { moveSuccessful = false }
            }
        }
        if (moveSuccessful){
            updateFolderReference(recording, folderRef , newRecordingPath)
            updateFolderCount(recording.folder, folderRef)
        }
    }

    fun updateFolderCount(oldFolderRef: Int?, newFolderRef: Int?){
        var oldFolder: FolderEntity? = null
        var newFolder: FolderEntity? = null
        allFolders.value!!.forEach {folder ->
           oldFolderRef?.let { if(folder.uid == it){ oldFolder = folder } }
            newFolderRef?.let { if(folder.uid == it){ newFolder = folder } }
        }
        oldFolder?.let {
            val count = it.contentCount - 1
            repository.updateFolderCount(oldFolder!!.uid, count)
        }
        newFolder?.let {
            val count = it.contentCount + 1
            repository.updateFolderCount(newFolder!!.uid, count)
        }
    }

    private fun updateFolderReference(entryEntity: RecordingAndLabels, folderUid: Int?, newPath: String?){
        var recordingPath = entryEntity.recordingPath
        if (newPath != null){ recordingPath = newPath}
        repository.updateFolderRef(entryEntity.uid, folderUid, recordingPath)
    }

    fun toggleFolderExpansion(folder: FolderEntity){
       val isExpanded = !folder.isExpanded
        repository.updateFolderExpansion(folder.uid, isExpanded)
    }

    fun getOldFolder(uid: Int) {
        allFolders.value!!.forEach { if (it.uid == uid){
            folderToBeEdited = it
            return
            }
        }
    }

}
