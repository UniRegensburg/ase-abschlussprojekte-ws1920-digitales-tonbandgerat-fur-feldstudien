package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.lifecycle.*
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.RecordingAndLabels
import de.ur.mi.audidroid.models.RecordingAndMarkTuple
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.ShareHelper
import java.io.File
import java.util.*
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
    private val allRecordings: LiveData<List<EntryEntity>> = repository.getAllRecordings()
    private val allRecordingsWithLabels: LiveData<List<RecordingAndLabels>> =
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

    val _sortMode = MutableLiveData<Int?>()
    val sortMode: LiveData<Int?>
        get() = _sortMode

    private val _createFolderDialog = MutableLiveData<Boolean>()
    val createFolderDialog: LiveData<Boolean>
        get() = _createFolderDialog

    val _createFilterDialog = MutableLiveData<Boolean>()
    val createFilterDialog: LiveData<Boolean>
        get() = _createFilterDialog

    private val _createConfirmDialog = MutableLiveData<Boolean>()
    val createConfirmDialog: LiveData<Boolean>
        get() = _createConfirmDialog

    private val _createRenameDialog = MutableLiveData<Boolean>()
    val createRenameDialog: LiveData<Boolean>
        get() = _createRenameDialog

    val _createAlertDialog = MutableLiveData<Boolean>()
    val createAlertDialog: MutableLiveData<Boolean>
        get() = _createAlertDialog

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

    fun rename(recordingAndLabels: RecordingAndLabels){
        recording = recordingAndLabels
        _createRenameDialog.value = true
    }

    fun saveRename(rec: RecordingAndLabels, newName: String){
        _createRenameDialog.value = false
        if (!validNameInput(newName)) {
            errorMessage = res.getString(R.string.dialog_invalid_name)
            _createRenameDialog.value = true
            return
        }
        if (newName.length > res.getInteger(R.integer.max_name_length)) {
            errorMessage = res.getString(R.string.dialog_name_length)
            _createRenameDialog.value = true
            return
        }
        val name = checkVariables(newName)
        if(name != rec.recordingName){
            val newPath = rec.recordingPath.subSequence(0, rec.recordingPath.length - (rec.recordingName.length + context.getString(R.string.suffix_audio_file).length)).toString() + name + context.getString(R.string.suffix_audio_file)
            if(File(newPath).exists()){
                errorMessage = res.getString(R.string.rename_dialog_error_already_exist)
                _createRenameDialog.value = true
                return
            }
            val newFile = File(newPath)
            File(rec.recordingPath).copyTo(newFile)
            File(rec.recordingPath).delete()
            repository.updateNameAndPath(rec.uid, name, newPath, rec.date)
        }
        recording = null
    }

    private fun validNameInput(name: String): Boolean {
        return Pattern.compile("^[a-zA-Z0-9_{}-]+$").matcher(name).matches()
    }

    private fun checkVariables(nameParam: String): String{
        var name = nameParam
        if (name.contains("{date}")) {
            name = name.replace(
                "{date}", java.lang.String.format(
                    "%s",
                    android.text.format.DateFormat.format(
                        "yyyy-MM-dd",
                        Calendar.getInstance(Locale.getDefault())
                    )
                )
            )
        }
        if (name.contains("{time}")) {
            name = name.replace(
                "{time}", java.lang.String.format(
                    "%s",
                    android.text.format.DateFormat.format(
                        "HH-mm",
                        Calendar.getInstance(Locale.getDefault())
                    )
                )
            )
        }
        return name
    }

    fun cancelRename(){
        errorMessage = null
        recording = null
        _createRenameDialog.value = false
    }

    fun cancelFilterDialog(){
        recording = null
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
        return if (params.isNotEmpty()) {
            list?.forEach { recMarkTuple ->
                filteredRef.add(recMarkTuple.first)
                params.forEach { param ->
                    if (!recMarkTuple.second.contains(param)) {
                        filteredRef.remove(recMarkTuple.first)
                    }
                }
            }
            filteredRef
        } else {
            null
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
            if (labels.isNotEmpty() || marks.isNotEmpty() || !nameInput.isNullOrEmpty()) {
                val matchedLabels =
                    matchLabelsAndRecordings(allRecordingsWithLabels.value!!, labels)
                val matchedMarks = matchMarksAndRecordings(marks)
                val displayList = combineFilterParams(matchedLabels, matchedMarks, nameInput)
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

    fun setSorting(mode: Int?) {
        removeSortedRecordingSources()
        when (mode) {
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

    fun openFolderMenu(view: View) = PopupMenu(view.context, view).run {
            menuInflater.inflate(R.menu.popup_menu_add_folder, menu)
            setOnMenuItemClickListener { item ->
                when (item.toString()) {
                    context.getString(R.string.folder_internal) -> _createFolderDialog.value = true
                    context.getString(R.string.folder_external) -> createExternalFolder()
                }
               true
            }
        show()
    }

    fun createFolder(folderName : String){
        _createFolderDialog.value = false
        if(folderName.isEmpty()){
            errorMessage = context.getString(R.string.folder_dialog_error_no_name)
            _createFolderDialog.value = true
            return
        }

    }

    private fun createExternalFolder(){
        //TODO: Use Pathfinder
    }

    private fun createInternalFolder(){
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
