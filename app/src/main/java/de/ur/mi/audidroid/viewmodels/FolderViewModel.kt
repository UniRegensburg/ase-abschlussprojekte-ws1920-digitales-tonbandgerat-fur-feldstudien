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
    private val res: Resources = context.resources
    private val _createAlertDialog = MutableLiveData<Boolean>()
    private val _createConfirmDialog = MutableLiveData<Boolean>()
    private lateinit var frameLayout: FrameLayout
    val allFolders: LiveData<List<FolderEntity>> = repository.getAllFolders()
    var allInternalFolders: LiveData<List<FolderEntity>> = repository.getFolderByStorage(false)
    var allExternalFolders: LiveData<List<FolderEntity>> = repository.getFolderByStorage(true)
    var allInternalFoldersSorted = MediatorLiveData<List<FolderEntity>>()
    var allExternalFoldersSorted = MediatorLiveData<List<FolderEntity>>()
    var folderToBeCreated: Boolean? = null
    var dialogType: Int = R.string.confirm_dialog
    var errorMessage: String? = null
    var folderToBeEdited: FolderEntity? = null


    private var _showSnackbarEvent = MutableLiveData<String>()

    val createAlertDialog: MutableLiveData<Boolean>
        get() = _createAlertDialog

    val createConfirmDialog: MutableLiveData<Boolean>
        get() = _createConfirmDialog

    val showSnackbarEvent: LiveData<String>
        get() = _showSnackbarEvent

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = ""
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

    // The lists with the content for the two Folder RecyclerViews are prepared and curated.
    fun initFolderSorting(){
        if (allInternalFoldersSorted.value == null){
            allInternalFoldersSorted.addSource(allInternalFolders){
                allInternalFoldersSorted.value = StorageHelper.getInternalFolderHierarchy(allInternalFolders.value!!)
            }
        }
        if (allExternalFoldersSorted.value == null){
            allExternalFoldersSorted.addSource(allExternalFolders){
                allExternalFoldersSorted.value = allExternalFolders.value
            }
        }
    }

    // When the ImageButton is clicked, a PopupMenu opens.
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

    private fun onDeleteFolderClicked( folderEntity: FolderEntity){
        _createConfirmDialog.value = true
        dialogType = R.string.confirm_dialog
        folderToBeEdited = folderEntity
    }

    // Handles the reference in the Database for the ActivityForResult result.
    fun handleActivityResult(result: String){
        val folders = allFolders.value
        val allFolderPaths: ArrayList<String> = arrayListOf()
        folders!!.forEach {
            if(it.dirPath != null){
                allFolderPaths.add(it.dirPath!!) }
            }
        val existingFolder = StorageHelper.checkExternalFolderReference(allFolderPaths, result)
        if (existingFolder == null) {
            StorageHelper.createFolderFromUri(repository, result)
        }
    }

    fun deleteFolderFromDB(folderList: MutableList<FolderEntity>) {
        folderList.forEach { repository.deleteFolder(it) }
        _showSnackbarEvent.value = res.getString(R.string.delete)
        folderToBeEdited = null
    }


    //Binds the add-internal_folder-Button in files_fragment.xml.
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
            errorMessage = res.getString(R.string.dialog_folder_invalid_name)
            _createAlertDialog.value = true
            return
        }
        if (!folderNameUnique(nameInput)){
            errorMessage = res.getString(R.string.dialog_folder_name_occurance)
            _createAlertDialog.value = true
            return
        }
        createFolderInDB(nameInput, parentFolder)
        _showSnackbarEvent.value = res.getString(R.string.create_folder)
    }

    private fun createFolderInDB(nameInput: String, parentFolder: FolderEntity?){
        repository.insertFolder(StorageHelper.createInternalFolderEntity(nameInput,parentFolder))
        folderToBeEdited = null
    }



    //handels the move of a folder from one folder to another
    fun onEntryMoveFolderClicked(entryEntity: EntryEntity, folderUid: Int?, folderPath: String?){

        var newPath: String? = null
        if (folderPath != null){
            if (folderPath.contains(res.getString(R.string.content_uri_prefix))||
                entryEntity.recordingPath.contains(res.getString(R.string.content_uri_prefix))){
                newPath =  StorageHelper.moveEntryStorage(context, entryEntity, folderPath)
            }
        }else{
            if(entryEntity.recordingPath.contains(res.getString(R.string.content_uri_prefix))) {
                errorMessage = res.getString(R.string.dialog_external_to_internal)
                folderToBeCreated = true
                _createConfirmDialog.value = true
                return
                }
        }
        updateEntryFolderInDB(entryEntity,folderUid, newPath)
    }

    fun updateEntryFolderInDB(entryEntity: EntryEntity, folderUid: Int?, newPath: String?){

        var recordingPath = entryEntity.recordingPath
        if (newPath != null){ recordingPath = newPath}

        val updatedEntry = EntryEntity(entryEntity.uid,
            entryEntity.recordingName, recordingPath,
            entryEntity.date, entryEntity.duration, folderUid, entryEntity.labels)
        repository.updateEntry(updatedEntry)
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
}
