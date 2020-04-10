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
import de.ur.mi.audidroid.models.RecordingAndLabels
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
    var errorMessage: String? = null
    var recordingToBeMoved: RecordingAndLabels? = null
    var recording: RecordingAndLabels? = null
    var recordingToBeExported: RecordingAndLabels? = null

    val allRecordingsWithLabels: LiveData<List<RecordingAndLabels>> =
        repository.getAllRecordingsWithLabels()
    val allRecordings: LiveData<List<EntryEntity>> =
        repository.getAllRecordings()

    private val _createAlertConvertDialog = MutableLiveData<Boolean>()
    val createAlertConvertDialog: MutableLiveData<Boolean>
        get() = _createAlertConvertDialog

    private val _createAlertFolderDialog = MutableLiveData<Boolean>()
    val createAlertFolderDialog: MutableLiveData<Boolean>
        get() = _createAlertFolderDialog

    private val _createConfirmDialog = MutableLiveData<Boolean>()
    val createConfirmDialog: LiveData<Boolean>
        get() = _createConfirmDialog

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

    fun shareRecording(format: String) {
        ShareHelper.shareAudio(recordingToBeExported!!, format, context)
        _createAlertConvertDialog.value = false
    }

    fun delete(recordingAndLabels: RecordingAndLabels) {
        recording = recordingAndLabels
        _createConfirmDialog.value = true
    }

    fun deleteRecording(recordingAndLabels: RecordingAndLabels) {
        val deletedSuccessful = StorageHelper.deleteFile(context,
            recordingAndLabels.recordingPath, recordingAndLabels.recordingName)
        if (deletedSuccessful) {
            repository.deleteRecording(recordingAndLabels.uid)
            repository.deleteRecMarks(recordingAndLabels.uid)
            showSnackBar(
                String.format(
                    context.getString(R.string.recording_deleted),
                    recordingAndLabels.recordingName
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
        it: List<RecordingAndLabels>,
        array: ArrayList<RecordingAndLabels>
    ): ArrayList<RecordingAndLabels> {
        for (i in it.indices) {
            if (StorageHelper.checkFileExistence(context, it[i].recordingPath,
                    it[i].recordingName)){
                array.add(it[i])
            } else {
                repository.deleteRecording(it[i].uid)
                repository.deleteRecMarks(it[i].uid)
                repository.deleteRecLabels(it[i].uid)
            }
        }
        return array
    }

    /** Checks if a recording is allowed to be moved to the destination, i. e. catch and deny
     *  the attempt of moving an external file to the internal storage (via 'remove from folder').
     */
    fun recordingMoveValid(recordingAndLabels: RecordingAndLabels, destFolder: Int?) {
        _createAlertFolderDialog.value = false
        if (destFolder == null) {
            if (recordingAndLabels.recordingPath.startsWith(context.getString(R.string.content_uri_prefix))) {
                errorMessage = context.getString(R.string.dialog_invalide_enty_move)
                _createAlertFolderDialog.value = true
                return
            }
        }
        errorMessage = null
        recordingToBeMoved = null
    }

    fun deleteEntriesInFolders(folderRefs: List<Int>) {
        if (folderRefs.isNotEmpty()){
            folderRefs.forEach {ref ->
                allRecordings.value!!.forEach {recording ->
                    if (recording.folder == ref) {
                        val recordingWithLabel = allRecordingsWithLabels.value!!
                        recordingWithLabel.forEach {
                            if (recording.uid == it.uid){ deleteRecording(it) }
                        }
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
