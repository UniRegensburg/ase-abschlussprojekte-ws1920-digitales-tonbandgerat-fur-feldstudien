package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.widget.FrameLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.RecordingEntity
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.ShareHelper
import java.io.File

/**
 * ViewModel for FilesFragment.
 * @author: Theresa Strohmeier
 */
class FilesViewModel(dataSource: Repository, application: Application) :
    AndroidViewModel(application) {

    private val repository = dataSource
    private val context = getApplication<Application>().applicationContext
    private val allRecordings: LiveData<List<EntryEntity>> = repository.getAllRecordings()
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
            repository.deleteRecLabels(recordingAndLabels.uid)
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
            val file = File(it[i].recordingPath)
            if (file.exists() && !it[i].recordingName.contains((context.getString(R.string.filename_trimmed_recording)))) {
                array.add(it[i])
            } else {
                repository.deleteRecording(it[i].uid)
                repository.deleteRecMarks(it[i].uid)
                repository.deleteRecLabels(it[i].uid)
            }
        }
        return array
    }

    // Navigation to the PlayerFragment
    private val _navigateToPlayerFragment = MutableLiveData<MutableList<String>>()
    val navigateToPlayerFragment
        get() = _navigateToPlayerFragment

    fun onRecordingClicked(recordingId: Int, recordingName: String, recordingPath: String) {
        val recording = mutableListOf<String>()
        recording.add(0, recordingId.toString())
        recording.add(1, recordingName)
        recording.add(2, recordingPath)
        _navigateToPlayerFragment.value = recording
    }

    fun onPlayerFragmentNavigated() {
        _navigateToPlayerFragment.value = null
    }

    fun cancelExporting() {
        recordingToBeExported = null
        _createAlertDialog.value = false
    }
}
