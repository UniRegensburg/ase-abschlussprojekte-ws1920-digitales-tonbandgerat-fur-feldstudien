package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.lifecycle.*
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
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
    private val res = context.resources
    private val allRecordings: LiveData<List<EntryEntity>> = repository.getAllRecordings()
    private val allRecordingsWithLabels: LiveData<List<RecordingAndLabels>> =
        repository.getAllRecordingsWithLabels()
    private val allRecordingsWithLabelsOrderName: LiveData<List<RecordingAndLabels>> =
        repository.getAllRecWithLabelsOrderName()
    private val allRecordingsWithLabelsOrderDate: LiveData<List<RecordingAndLabels>> =
        repository.getAllRecWithLabelsOrderDate()
    private val allRecordingsWithLabelsOrderDuration: LiveData<List<RecordingAndLabels>> =
        repository.getAllRecWithLabelsOrderDuration()
    val displayRecordings = MediatorLiveData<List<RecordingAndLabels>>()
    private lateinit var frameLayout: FrameLayout
    var errorMessage: String? = null
    var recording: RecordingAndLabels? = null
    var recordingToBeExported: RecordingAndLabels? = null

    val _sortModus = MutableLiveData<Int?>()
    val sortModus: LiveData<Int?>
        get() = _sortModus

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

    // Set sorted source for recording display
    private fun removeSortedRecordingSources(){
        displayRecordings.removeSource(allRecordingsWithLabels)
        displayRecordings.removeSource(allRecordingsWithLabelsOrderName)
        displayRecordings.removeSource(allRecordingsWithLabelsOrderDate)
        displayRecordings.removeSource(allRecordingsWithLabelsOrderDuration)
    }

    fun setSearchResult(search: String){
        removeSortedRecordingSources()
        displayRecordings.addSource(allRecordingsWithLabels){
            val displayList = mutableListOf<RecordingAndLabels>()
            allRecordingsWithLabels.value?.forEach { recording ->
                if (recording.recordingName.contains(search,true)){
                    displayList.add(recording)
                }
            }
            displayRecordings.value = displayList
        }
    }

    fun setSorting(modus: Int?){
        removeSortedRecordingSources()
        when (modus){
            res.getInteger(R.integer.sort_by_name) -> {
                displayRecordings.addSource(allRecordingsWithLabelsOrderName){
                    displayRecordings.value = allRecordingsWithLabelsOrderName.value
                }
            }
            res.getInteger(R.integer.sort_by_date) -> {
                displayRecordings.addSource(allRecordingsWithLabelsOrderDate){
                    displayRecordings.value = allRecordingsWithLabelsOrderDate.value
                }
            }
            res.getInteger(R.integer.sort_by_duration) -> {
                displayRecordings.addSource(allRecordingsWithLabelsOrderDuration){
                    displayRecordings.value = allRecordingsWithLabelsOrderDuration.value
                }
            }
            else -> {
                displayRecordings.addSource(allRecordingsWithLabels){
                    displayRecordings.value = allRecordingsWithLabels.value
                }
            }
        }
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
