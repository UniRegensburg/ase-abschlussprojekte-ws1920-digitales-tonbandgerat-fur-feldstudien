package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.net.Uri
import android.widget.FrameLayout
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.*
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.*
import de.ur.mi.audidroid.utils.ShareHelper
import java.io.File
import java.util.regex.Pattern
import kotlin.collections.ArrayList

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
    private val allRecordingsWithLabelsOrderName: LiveData<List<RecordingAndLabels>> =
        repository.getAllRecWithLabelsOrderName()
    private val allRecordingsWithLabelsOrderDate: LiveData<List<RecordingAndLabels>> =
        repository.getAllRecWithLabelsOrderDate()
    private val allRecordingsWithLabelsOrderDuration: LiveData<List<RecordingAndLabels>> =
        repository.getAllRecWithLabelsOrderDuration()
    val allRecordingsWithMarker: LiveData<List<RecordingAndMarkTuple>> =
        repository.getRecordingsAndMarkerType()
    val displayRecordings = MediatorLiveData<List<RecordingAndLabels>>()
    private lateinit var frameLayout: FrameLayout
    var errorMessage: String? = null
    var recording: RecordingAndLabels? = null
    var recordingToBeExported: RecordingAndLabels? = null

    val _sortModus = MutableLiveData<Int?>()
    val sortModus: LiveData<Int?>
        get() = _sortModus

    val _createFilterDialog = MutableLiveData<Boolean>()
    val createFilterDialog: LiveData<Boolean>
        get() = _createFilterDialog

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

    private var _filterEmpty = MutableLiveData<Boolean>()
    val filterEmpty: LiveData<Boolean>
        get() = _filterEmpty


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

    fun cancelNamingDialog() {
        _createRenameDialog.value = false
        recording = null
    }

    fun cancelFilterDialog() {
        _createFilterDialog.value = false
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
                if (!it[i].recordingName.contains((context.getString(R.string.filename_trimmed_recording)))) {
                    array.add(it[i])
                } else if (it[i].recordingName.contains((context.getString(R.string.filename_trimmed_recording)))) {
                    File(it[i].recordingPath).delete()
                    repository.deleteRecording(it[i].uid)
                    repository.deleteRecMarks(it[i].uid)
                    repository.deleteRecLabels(it[i].uid)
                }
            } else if (!file.exists() && !it[i].recordingName.contains((context.getString(R.string.filename_trimmed_recording)))) {
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
            if (recording.recordingPath.startsWith(res.getString(R.string.content_uri_prefix))){
                renameExternalFile(recording.recordingPath, recording.recordingName, nameInput!!)
            }
            updateNameInDB(recording.uid, nameInput!!)
        }
    }

    private fun renameExternalFile(path: String, name: String, userInput: String){
        val treeUri = Uri.parse(path)
        var fileName = name + res.getString(R.string.suffix_audio_file)
        val file = DocumentFile.fromTreeUri(context, treeUri)!!.findFile(fileName)
        fileName = userInput +  res.getString(R.string.suffix_audio_file)
        file!!.renameTo(fileName)
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

    private fun recordingNameAlreadyTaken(recordingName: String): Boolean {
        return repository.getRecordingByName(recordingName).isNotEmpty()
    }

    // Set sorted source for recording display
    private fun removeSortedRecordingSources() {
        displayRecordings.removeSource(allRecordingsWithLabels)
        displayRecordings.removeSource(allRecordingsWithLabelsOrderName)
        displayRecordings.removeSource(allRecordingsWithLabelsOrderDate)
        displayRecordings.removeSource(allRecordingsWithLabelsOrderDuration)
    }

    private fun matchLabelsAndRecordings(
        recordingList: List<RecordingAndLabels>,
        params: List<String>
    ): List<RecordingAndLabels>? {
        if (params.isNotEmpty()) {
            var filteredRecordings = arrayListOf<RecordingAndLabels>()
            recordingList.forEach { recording ->
                filteredRecordings.add(recording)
                if (recording.labels != null) {
                    params.forEach { param ->
                        if (!recording.labels.contains(param)) {
                            filteredRecordings.remove(recording)
                        }
                    }
                } else {
                    filteredRecordings.remove(recording)
                }
            }
            return filteredRecordings
        } else {
            return null
        }
    }

    private fun adaptRecordingMarkLiveData(reference: List<RecordingAndMarkTuple>): List<Pair<Int, MutableList<String>>>? {
        if (reference.isNotEmpty()) {
            val refList = mutableListOf<Pair<Int, MutableList<String>>>()
            var alreadyInList: Boolean
            reference.forEach { ref ->
                val pair = Pair(ref.recordingId, mutableListOf(ref.markerName))
                if (refList.isEmpty()) {
                    refList.add(pair)
                } else {
                    alreadyInList = false
                    refList.forEach {
                        if (it.first == ref.recordingId) {
                            it.second.add(ref.markerName)
                            alreadyInList = true
                        }
                    }
                    if (!alreadyInList) {
                        refList.add(pair)
                    }
                }
            }
            return refList
        } else {
            return null
        }
    }

    private fun matchMarksAndRecordings(params: List<String>): List<Int>? {
        val filteredRef = arrayListOf<Int>()
        val list = adaptRecordingMarkLiveData(allRecordingsWithMarker.value!!)
        if (params.isNotEmpty()) {
            list?.forEach { recMarkTuple ->
                filteredRef.add(recMarkTuple.first)
                params.forEach { param ->
                    if (!recMarkTuple.second.contains(param)) {
                        filteredRef.remove(recMarkTuple.first)
                    }
                }
            }
            return filteredRef
        } else {
            return null
        }
    }

    private fun combineFilterParams(
        matchedLabels: List<RecordingAndLabels>?, matchedMarkers: List<Int>?,
        nameInput: String?
    ): List<RecordingAndLabels> {
        var filteredResult = allRecordingsWithLabels.value!!
        var matchedList = mutableListOf<RecordingAndLabels>()
        matchedLabels?.let { filteredResult = matchedLabels }
        matchedMarkers?.let {
            filteredResult.forEach { rec ->
                if (matchedMarkers.contains(rec.uid)) {
                    matchedList.add(rec)
                }
            }
            filteredResult = matchedList
        }
        nameInput?.let {
            matchedList = mutableListOf<RecordingAndLabels>()
            filteredResult.forEach {
                if (it.recordingName.contains(nameInput, true)) {
                    matchedList.add(it)
                }
            }
            filteredResult = matchedList
        }
        return filteredResult
    }

    fun setFilterResult(labels: List<String>, marks: List<String>, nameInput: String?) {
        removeSortedRecordingSources()
        displayRecordings.addSource(allRecordingsWithLabels) {
            if (!labels.isEmpty() || !marks.isEmpty() || !nameInput.isNullOrEmpty()) {
                val matchedLabels =
                    matchLabelsAndRecordings(allRecordingsWithLabels.value!!, labels)
                val matchedMarks = matchMarksAndRecordings(marks)
                var displayList = combineFilterParams(matchedLabels, matchedMarks, nameInput)
                displayRecordings.value = displayList
            } else {
                displayRecordings.value = allRecordingsWithLabels.value!!
            }
            _filterEmpty.value = displayRecordings.value!!.isEmpty()
        }
    }

    fun setSearchResult(search: String) {
        removeSortedRecordingSources()
        displayRecordings.addSource(allRecordingsWithLabels) {
            val displayList = mutableListOf<RecordingAndLabels>()
            allRecordingsWithLabels.value?.forEach { recording ->
                if (recording.recordingName.contains(search, true)) {
                    displayList.add(recording)
                }
            }
            displayRecordings.value = displayList
        }
    }

    fun setSorting(modus: Int?) {
        removeSortedRecordingSources()
        when (modus) {
            res.getInteger(R.integer.sort_by_name) -> {
                displayRecordings.addSource(allRecordingsWithLabelsOrderName) {
                    displayRecordings.value = allRecordingsWithLabelsOrderName.value
                }
            }
            res.getInteger(R.integer.sort_by_date) -> {
                displayRecordings.addSource(allRecordingsWithLabelsOrderDate) {
                    displayRecordings.value = allRecordingsWithLabelsOrderDate.value
                }
            }
            res.getInteger(R.integer.sort_by_duration) -> {
                displayRecordings.addSource(allRecordingsWithLabelsOrderDuration) {
                    displayRecordings.value = allRecordingsWithLabelsOrderDuration.value
                }
            }
            else -> {
                displayRecordings.addSource(allRecordingsWithLabels) {
                    displayRecordings.value = allRecordingsWithLabels.value
                }
            }
        }
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
