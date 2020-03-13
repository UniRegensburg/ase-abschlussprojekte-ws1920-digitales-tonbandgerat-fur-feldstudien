package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.widget.FrameLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.ShareHelper
import de.ur.mi.audidroid.utils.StorageHelper
import java.io.File

/**
 * ViewModel for FilesFragment.
 * @author: Theresa Strohmeier
 */
class FilesViewModel(dataSource: Repository, application: Application) :
    AndroidViewModel(application) {

    private val repository = dataSource
    private val context = getApplication<Application>().applicationContext
    private lateinit var frameLayout: FrameLayout
    var recordingToBeExported: EntryEntity? = null
    var recordingToBeMoved: EntryEntity? = null
    val allRecordings: LiveData<List<EntryEntity>> = repository.getAllRecordings()
    val allRecordingsWithNoFolder: LiveData<List<EntryEntity>> = repository.getRecordingWithNoFolder()
    var errorMessage: String? = null
    var recording: EntryEntity? = null


    //meinz
    private val _createAlertConvertDialog = MutableLiveData<Boolean>()
    val createAlertConvertDialog: MutableLiveData<Boolean>
        get() = _createAlertConvertDialog
    //meinz
    private val _createAlertFolderDialog = MutableLiveData<Boolean>()
    val createAlertFolderDialog: MutableLiveData<Boolean>
        get() = _createAlertFolderDialog


    //master -> wird beim löschen von Einträgen aufgerufen
    private val _createConfirmDialog = MutableLiveData<Boolean>()
    val createConfirmDialog: LiveData<Boolean>
        get() = _createConfirmDialog
    //master
    //val _createAlertDialog = MutableLiveData<Boolean>()
    //val createAlertDialog: MutableLiveData<Boolean>
    //    get() = _createAlertDialog



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



    /*HEAD
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
    }*/

    fun shareRecording(format: String) {
        ShareHelper.shareAudio(recordingToBeExported!!, format, context)
        _createAlertConvertDialog.value = false
    }



    /*<<<<<< HEAD
    private fun delete(entryEntity: EntryEntity) {
        val deletedSuccessful = StorageHelper.deleteFile(context, entryEntity)
        if (deletedSuccessful) {
            repository.delete(entryEntity)
            _showSnackbarEvent.value = true*/
    fun delete(entryEntity: EntryEntity) {
        recording = entryEntity
        _createConfirmDialog.value = true
    }

    fun deleteRecording(entryEntity: EntryEntity) {
        val file = File(entryEntity.recordingPath)
        if (file.delete()) {
            repository.deleteRecording(entryEntity)
            showSnackBar(
                String.format(
                    context.getString(R.string.recording_deleted),
                    entryEntity.recordingName
                )
            )
            recording = null
        } else {
            showSnackBar(R.string.error_message_file_cannot_be_deleted.toString())
        }
    }

    fun cancelSaving() {
        errorMessage = null
        recording = null
        _createConfirmDialog.value = false
    }

    fun initializeFrameLayout(frameLayout: FrameLayout) {
        this.frameLayout = frameLayout
    }

    private fun showSnackBar(text: String) {
        Snackbar.make(frameLayout, text, Snackbar.LENGTH_LONG).show()
    }

    fun checkExistence(
        it: List<EntryEntity>,
        array: ArrayList<EntryEntity>
    ): ArrayList<EntryEntity> {
        for (i in it.indices) {
            val file = File(it[i].recordingPath)
            if (file.exists()) {
                array.add(it[i])
            } else {
                repository.deleteRecording(it[i])
                repository.deleteRecMarks(it[i].uid)
                repository.deleteRecLabels(it[i].uid)
            }
        }
        return array
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


    fun deleteEntriesInInternalFolders(folderRefs: List<Int>) {
        if (folderRefs.isNotEmpty()){
            folderRefs.forEach {ref ->
                allRecordings.value!!.forEach {
                    if (it.folder == ref) {
                        deleteRecording(it)
                    }
                }
            }
        }
    }

    fun getAllRecordingsByFolder(folder : FolderEntity): LiveData<List<EntryEntity>>{
        return repository.getRecordingByFolder(folder.uid)
    }

    // Navigation to the PlayerFragment
    private val _navigateToPlayerFragment = MutableLiveData<Int>()
    val navigateToPlayerFragment
        get() = _navigateToPlayerFragment

    fun onRecordingClicked(recordingId: Int) {
        _navigateToPlayerFragment.value = recordingId
    }

    fun onPlayerFragmentNavigated() {
        _navigateToPlayerFragment.value = null
    }

    fun cancelExporting() {
        recordingToBeExported = null
        _createAlertConvertDialog.value = false
    }
}
