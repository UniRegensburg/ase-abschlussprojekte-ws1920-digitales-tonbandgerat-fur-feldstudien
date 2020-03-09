package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.ShareHelper
import de.ur.mi.audidroid.utils.StorageHelper

/**
 * ViewModel for FilesFragment.
 * @author: Theresa Strohmeier
 */
class FilesViewModel(dataSource: Repository, application: Application) :
    AndroidViewModel(application) {

    private val repository = dataSource
    private val context = getApplication<Application>().applicationContext
    private var recordingToBeExported: EntryEntity? = null
    var recordingToBeMoved: EntryEntity? = null
    val allRecordings: LiveData<List<EntryEntity>> = repository.getAllRecordings()
    val allRecordingsWithNoFolder: LiveData<List<EntryEntity>> = repository.getRecordingWithNoFolder()
    var folderReferenceList = MutableLiveData<List<Int>>()
    var errorMessage: String? = null


    private val _createAlertFolderDialog = MutableLiveData<Boolean>()

    val createAlertFolderDialog: MutableLiveData<Boolean>
        get() = _createAlertFolderDialog

    private val _createAlertConvertDialog = MutableLiveData<Boolean>()

    val createAlertConvertDialog: MutableLiveData<Boolean>
        get() = _createAlertConvertDialog

    private var _showSnackbarEvent = MutableLiveData<Boolean>()

    val showSnackbarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = null
    }

    fun cancelFolderDialog(){
        _createAlertFolderDialog.value = false
    }

    // If there are no recordings in the database, a TextView is displayed.
    val empty: LiveData<Boolean> = Transformations.map(allRecordings) {
        it.isEmpty()
    }

    // When the ImageButton is clicked, a PopupMenu opens.
    fun onButtonClicked(entryEntity: EntryEntity, view: View) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete_recording ->
                    delete(entryEntity)
                R.id.action_move_recording ->{
                    recordingToBeMoved = entryEntity
                    _createAlertFolderDialog.value = true
                }
                R.id.action_share_recording -> {
                    recordingToBeExported = entryEntity
                    _createAlertConvertDialog.value = true
                }
            }
            true
        }
        popupMenu.show()
    }

    fun shareRecording(format: String) {
        ShareHelper.shareAudio(recordingToBeExported!!, format, context)
        _createAlertConvertDialog.value = false
    }

    private fun delete(entryEntity: EntryEntity) {
        val deletedSuccessful = StorageHelper.deleteFile(context, entryEntity)
        if (deletedSuccessful) {
            repository.delete(entryEntity)
            _showSnackbarEvent.value = true
        }
    }


    /** Checks if a recording is allowed to be moved to the destination, i. e. catch and deny
     *  the attempt of moving an external file to the internal storage (via 'remove from folder').
     */
    fun recordingMoveValid(entryEntity: EntryEntity, destFolder: Int?){
        _createAlertFolderDialog.value = false
        if (destFolder == null) {
            if (entryEntity.recordingPath.startsWith(context.getString(R.string.content_uri_prefix))) {
                errorMessage = context.getString(R.string.dialog_invalide_enty_move)
                _createAlertFolderDialog.value = true
                return
            }
        }
        errorMessage = null
        recordingToBeMoved = null
    }


    fun deleteEntriesInInternalFolders() {
        val folderList = folderReferenceList.value
        if (folderList!!.isNotEmpty()){
            folderList.forEach {ref ->
                allRecordings.value!!.forEach {
                    if (it.folder == ref) {
                        delete(it)
                    }
                }
            }
        }
        folderList.forEach { folder ->

        }
    }

    fun getAllRecordingsByFolder(folder : FolderEntity): LiveData<List<EntryEntity>>{
        return repository.getRecordingByFolder(folder.uid)
    }

    // Navigation to the PlayerFragment
    private val _navigateToPlayerFragment = MutableLiveData<String>()
    val navigateToPlayerFragment
        get() = _navigateToPlayerFragment

    fun onRecordingClicked(recordingPath: String) {
        _navigateToPlayerFragment.value = recordingPath
    }

    fun onPlayerFragmentNavigated() {
        _navigateToPlayerFragment.value = null
    }

    fun cancelExporting() {
        recordingToBeExported = null
        _createAlertConvertDialog.value = false
    }
}
