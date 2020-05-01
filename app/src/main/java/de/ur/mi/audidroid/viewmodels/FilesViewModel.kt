package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.widget.FrameLayout
import androidx.lifecycle.*
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.*
import de.ur.mi.audidroid.utils.ShareHelper
import de.ur.mi.audidroid.utils.StorageHelper
import java.io.File
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
    val folderRecordings = MediatorLiveData<List<RecordingAndLabels>>()

    private lateinit var frameLayout: FrameLayout
    var errorMessage: String? = null
    var recordingToBeMoved: RecordingAndLabels? = null
    var recording: RecordingAndLabels? = null
    var recordingToBeExported: RecordingAndLabels? = null

    private val _createAlertConvertDialog = MutableLiveData<Boolean>()
    val createAlertConvertDialog: MutableLiveData<Boolean>
        get() = _createAlertConvertDialog

    private val _createAlertFolderDialog = MutableLiveData<Boolean>()
    val createAlertFolderDialog: MutableLiveData<Boolean>
        get() = _createAlertFolderDialog

    val _sortModus = MutableLiveData<Int?>()
    val sortModus: LiveData<Int?>
        get() = _sortModus

    val _createFilterDialog = MutableLiveData<Boolean>()
    val createFilterDialog: LiveData<Boolean>
        get() = _createFilterDialog


    private val _createConfirmDialog = MutableLiveData<Boolean>()
    val createConfirmDialog: LiveData<Boolean>
        get() = _createConfirmDialog

    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackbarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    private var _filterEmpty = MutableLiveData<Boolean>()
    val filterEmpty: LiveData<Boolean>
        get() = _filterEmpty


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

    fun snackbarInvalidEntry(){
        showSnackBar(context.getString(R.string.no_file_at_location))
    }

    fun deleteRecording(recordingAndLabels: RecordingAndLabels) {
        val deletedSuccessful = StorageHelper.deleteFile(context,
            recordingAndLabels.recordingPath, recordingAndLabels.recordingName)
        if (deletedSuccessful) {
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

    /** Checks if a recording is allowed to be moved to the destination, i. e. catch and deny
     *  the attempt of moving an external file to the internal storage (via 'remove from folder').
     */
    fun recordingMoveValid(recordingAndLabels: RecordingAndLabels, destFolder: Int?):Boolean {
        _createAlertFolderDialog.value = false
        if (destFolder == null) {
            if (recordingAndLabels.recordingPath.startsWith(context.getString(R.string.content_uri_prefix))) {
                errorMessage = context.getString(R.string.dialog_invalide_enty_move)
                _createAlertFolderDialog.value = true
                return false
            }
        }
        errorMessage = null
        recordingToBeMoved = null
        return true
    }

    fun deleteEntriesInFolders(folderRefs: List<Int>) {
        if (folderRefs.isNotEmpty()) {
            folderRefs.forEach { ref ->
                allRecordings.value!!.forEach { recording ->
                    if (recording.folder == ref) {
                        val recordingWithLabel = allRecordingsWithLabels.value!!
                        recordingWithLabel.forEach {
                            if (recording.uid == it.uid) {
                                deleteRecording(it)
                            }
                        }
                    }
                }
            }
        }
    }

    // Set sorted source for recording display
    private fun removeSortedRecordingSources() {
        displayRecordings.removeSource(allRecordingsWithLabels)
        displayRecordings.removeSource(allRecordingsWithLabelsOrderName)
        displayRecordings.removeSource(allRecordingsWithLabelsOrderDate)
        displayRecordings.removeSource(allRecordingsWithLabelsOrderDuration)
    }
    private fun removeSortedFolderRecordingSources(){
        folderRecordings.removeSource(allRecordingsWithLabels)
        folderRecordings.removeSource(allRecordingsWithLabelsOrderName)
        folderRecordings.removeSource(allRecordingsWithLabelsOrderDate)
        folderRecordings.removeSource(allRecordingsWithLabelsOrderDuration)
    }

    private fun matchLabelsAndRecordings(
        recordingList: List<RecordingAndLabels>,
        params: List<String>
    ): List<RecordingAndLabels>? {
        if (params.isNotEmpty()) {
            val filteredRecordings = arrayListOf<RecordingAndLabels>()
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
                if (matchedMarkers.contains(rec.uid)) { matchedList.add(rec) }
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

    private fun getFilteredRecordings(labels: List<String>, marks: List<String>, nameInput: String?):List<RecordingAndLabels>{
        val matchedLabels = matchLabelsAndRecordings(allRecordingsWithLabels.value!!, labels)
        val matchedMarks = matchMarksAndRecordings(marks)
        return combineFilterParams(matchedLabels, matchedMarks, nameInput)
    }

    fun setFilterResult(labels: List<String>, marks: List<String>, nameInput: String?){
        val toBeFiltered = labels.isNotEmpty() || marks.isNotEmpty() || !nameInput.isNullOrEmpty()
        var filteredRecordings = listOf<RecordingAndLabels>()
        if (toBeFiltered) {
            filteredRecordings = getFilteredRecordings(labels, marks, nameInput)
        }

        removeSortedRecordingSources()
        removeSortedFolderRecordingSources()

        displayRecordings.addSource(allRecordingsWithLabels){
            if (toBeFiltered) {
                displayRecordings.value = filteredRecordings
                _filterEmpty.value = filteredRecordings.isEmpty()
            }else{
                displayRecordings.value = allRecordingsWithLabels.value!!
            }
        }
        folderRecordings.addSource(allRecordingsWithLabels){
            if (toBeFiltered) {
                folderRecordings.value = filteredRecordings
                _filterEmpty.value = filteredRecordings.isEmpty()
            }else{
                folderRecordings.value = allRecordingsWithLabels.value!!
            }
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
        removeSortedFolderRecordingSources()
        when (modus){
            res.getInteger(R.integer.sort_by_name) -> {
                displayRecordings.addSource(allRecordingsWithLabelsOrderName) {
                    displayRecordings.value = allRecordingsWithLabelsOrderName.value
                }
                folderRecordings.addSource(allRecordingsWithLabelsOrderName){
                    folderRecordings.value = allRecordingsWithLabelsOrderName.value
                }
            }
            res.getInteger(R.integer.sort_by_date) -> {
                displayRecordings.addSource(allRecordingsWithLabelsOrderDate) {
                    displayRecordings.value = allRecordingsWithLabelsOrderDate.value
                }
                folderRecordings.addSource(allRecordingsWithLabelsOrderDate){
                    folderRecordings.value = allRecordingsWithLabelsOrderDate.value
                }
            }
            res.getInteger(R.integer.sort_by_duration) -> {
                displayRecordings.addSource(allRecordingsWithLabelsOrderDuration) {
                    displayRecordings.value = allRecordingsWithLabelsOrderDuration.value
                }
                folderRecordings.addSource(allRecordingsWithLabelsOrderDuration){
                    folderRecordings.value = allRecordingsWithLabelsOrderDuration.value
                }
            }
            else -> {
                displayRecordings.addSource(allRecordingsWithLabels) {
                    displayRecordings.value = allRecordingsWithLabels.value
                }
                folderRecordings.addSource(allRecordingsWithLabels){
                    folderRecordings.value = allRecordingsWithLabels.value
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
        _createAlertConvertDialog.value = false
    }
}
