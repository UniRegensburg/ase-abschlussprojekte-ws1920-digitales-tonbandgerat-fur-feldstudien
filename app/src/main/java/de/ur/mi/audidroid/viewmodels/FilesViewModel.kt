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
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.ShareHelper
import java.io.File
import java.util.regex.Pattern

/**
 * ViewModel for FilesFragment.
 * @author: Theresa Strohmeier
 */
class FilesViewModel(dataSource: Repository, application: Application) :
    AndroidViewModel(application) {

    private val repository = dataSource
    private val context = getApplication<Application>().applicationContext
    private val res = context.resources
    val allRecordings: LiveData<List<EntryEntity>> = repository.getAllRecordings()
    val allRecordingsWithLabels: LiveData<List<RecordingAndLabels>> =
        repository.getAllRecordingsWithLabels()
    private lateinit var frameLayout: FrameLayout
    var errorMessage: String? = null
    var recording: RecordingAndLabels? = null
    var recordingToBeExported: RecordingAndLabels? = null

    private val _createConfirmDialog = MutableLiveData<Boolean>()
    val createConfirmDialog: LiveData<Boolean>
        get() = _createConfirmDialog

    val _createAlertDialog = MutableLiveData<Boolean>()
    val createAlertDialog: MutableLiveData<Boolean>
        get() = _createAlertDialog

    val _createRenameDialog = MutableLiveData<Boolean>()
    val createRenameDialog: MutableLiveData<Boolean>
        get() = _createRenameDialog

    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackbarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = null
    }

    // If there are no recordings in the database, a TextView is displayed.
    val empty: LiveData<Boolean> = Transformations.map(allRecordings) {
        it.isEmpty()
    }

    fun shareRecording(format: String) {
        ShareHelper.shareAudio(recordingToBeExported!!, format, context)
        _createAlertDialog.value = false
    }

    fun delete(recordingAndLabels: RecordingAndLabels) {
        recording = recordingAndLabels
        _createConfirmDialog.value = true
    }

    fun deleteRecording(recordingAndLabels: RecordingAndLabels) {
        val file = File(recordingAndLabels.recordingPath)
        if (file.delete()) {
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

    fun cancelNamingDialog(){
        _createRenameDialog.value = false
        recording = null
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
            val file = File(it[i].recordingPath)
            if (file.exists()) {
                array.add(it[i])
            } else {
                repository.deleteRecording(it[i].uid)
                repository.deleteRecMarks(it[i].uid)
                repository.deleteRecLabels(it[i].uid)
            }
        }
        return array
    }

    fun renameRecording(recording: RecordingAndLabels, nameInput: String?){
        _createRenameDialog.value = false
        this.recording = recording
        if (checkInput(nameInput)){
            updateNameInDB(recording.uid, nameInput!!)
        }
    }

    private fun updateNameInDB(recordingId: Int, recordingName: String){
        repository.updateRecordingName(recordingId, recordingName)
    }

    private fun checkInput(nameInput: String?): Boolean{
        if (nameInput.isNullOrEmpty()){
            _createRenameDialog.value = true
            return false
        }
        if (recordingNameAlreadyTaken(nameInput)){
            errorMessage = res.getString(R.string.dialog_already_exist)
            _createRenameDialog.value = true
            return false
        }
        if (!validName(nameInput)){
            errorMessage =  res.getString(R.string.dialog_invalid_name)
            _createRenameDialog.value = true
            return false
        }
        if (nameInput.length > res.getInteger(R.integer.max_name_length)){
            errorMessage = res.getString(R.string.dialog_name_length)
            _createRenameDialog.value = true
            return false
        }
        errorMessage = null
        return true
    }

    private fun validName(labelName: String): Boolean {
        return Pattern.compile("^[a-zA-Z0-9_{}-]+$").matcher(labelName).matches()
    }

    private fun recordingNameAlreadyTaken(recordingName: String): Boolean{
        return repository.getRecordingByName(recordingName).isNotEmpty()
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
        _createAlertDialog.value = false
    }
}
