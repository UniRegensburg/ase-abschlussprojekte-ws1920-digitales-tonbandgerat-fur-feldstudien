package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.models.RecordingAndMarkTuple
import de.ur.mi.audidroid.models.FolderEntity
import de.ur.mi.audidroid.models.FolderAssignmentEntity
import de.ur.mi.audidroid.utils.ShareHelper
import java.io.File
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import de.ur.mi.audidroid.models.RecordingAndLabels
import java.util.Calendar
import java.util.Locale

/**
 * ViewModel for FilesFragment.
 * @author: Theresa Strohmeier
 */
class FilesViewModel(dataSource: Repository, application: Application) :
    AndroidViewModel(application) {

    private val repository: Repository = dataSource
    private val context: Context = getApplication<Application>().applicationContext
    private val res: Resources = context.resources
    val allRecordingsWithMarker: LiveData<List<RecordingAndMarkTuple>> =
        repository.getRecordingsAndMarkerType()
    val allFolders: LiveData<List<FolderEntity>> = repository.getAllFolders()
    val displayRecordingsAndFolders = MediatorLiveData<List<Any>>()
    var folderToBeEdited: FolderEntity? = null
    var recordingToBeMoved: RecordingAndLabels? = null
    var deleteFolder = false

    private lateinit var frameLayout: FrameLayout
    var errorMessage: String? = null
    var recording: RecordingAndLabels? = null
    var recordingToBeExported: RecordingAndLabels? = null

    val _sortMode = MutableLiveData<Int?>()
    val sortMode: LiveData<Int?>
        get() = _sortMode

    private val _folderDialog = MutableLiveData<Boolean>()
    val folderDialog: LiveData<Boolean>
        get() = _folderDialog

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

    var _currentlyInFolder = MutableLiveData<Boolean>()
    val currentlyInFolder: LiveData<Boolean>
        get() = _currentlyInFolder

    var currentFolder: Int = 0

    var deleteRecordings = MediatorLiveData<List<RecordingAndLabels>>()

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = null
    }

    /**
     * If there are no recordings in the database, a TextView is displayed.
     */
    val empty: LiveData<Boolean> = Transformations.map(displayRecordingsAndFolders) {
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
            removeSortedRecordingSources(currentFolder)
            getRecordingsAndFolders()
        } else {
            showSnackBar(R.string.error_message_file_cannot_be_deleted.toString())
        }
    }

    fun rename(recordingAndLabels: RecordingAndLabels) {
        recording = recordingAndLabels
        _createRenameDialog.value = true
    }

    fun saveRename(rec: RecordingAndLabels, newName: String) {
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
        val name: String = checkVariables(newName)
        if (name != rec.recordingName) {
            val newPath: String = rec.recordingPath.subSequence(
                0,
                rec.recordingPath.length - (rec.recordingName.length + context.getString(R.string.suffix_audio_file).length)
            ).toString() + name + context.getString(R.string.suffix_audio_file)
            if (File(newPath).exists()) {
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

    private fun checkVariables(nameParam: String): String {
        var name: String = nameParam
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

    fun cancelRename() {
        errorMessage = null
        recording = null
        _createRenameDialog.value = false
    }

    fun cancelFilterDialog() {
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

    fun getCorrectList(
        it: List<Any>
    ): List<Any> {
        val recordings = ArrayList<RecordingAndLabels>()
        val folders = ArrayList<FolderEntity>()
        for (item: Any in it) {
            if (item is RecordingAndLabels) {
                val file = File(item.recordingPath)

                if (file.exists()) {
                    if (!item.recordingName.contains((context.getString(R.string.filename_trimmed_recording)))) {
                        if (currentlyInFolder.value == true) recordings.add(item)
                        else if (currentlyInFolder.value == false && notInFolder(item)) recordings.add(
                            item
                        )
                    } else if (item.recordingName.contains((context.getString(R.string.filename_trimmed_recording)))) {
                        File(item.recordingPath).delete()
                        repository.deleteRecording(item.uid)
                        repository.deleteRecMarks(item.uid)
                        repository.deleteRecLabels(item.uid)
                    }
                } else if (!file.exists() && !item.recordingName.contains((context.getString(R.string.filename_trimmed_recording)))) {
                    repository.deleteRecording(item.uid)
                    repository.deleteRecMarks(item.uid)
                    repository.deleteRecLabels(item.uid)
                }
            }
            if (item is FolderEntity) folders.add(item)
        }
        val all = ArrayList<Any>()
        all.addAll(recordings)
        all.addAll(folders)
        return all.toList()
    }

    private fun notInFolder(rec: RecordingAndLabels): Boolean {
        return repository.getFolderOfRecording(rec.uid) == null
    }

    private fun removeSortedRecordingSources(folder: Int) {
        if (_currentlyInFolder.value!!) {
            displayRecordingsAndFolders.removeSource(repository.getAllRecordingsWithLabelsOutsideFolder())
            displayRecordingsAndFolders.removeSource(
                repository.getAllRecWithLabelsOrderNameInFolder(
                    true,
                    folder
                )
            )
            displayRecordingsAndFolders.removeSource(
                repository.getAllRecWithLabelsOrderDurationInFolder(
                    true,
                    folder
                )
            )
            displayRecordingsAndFolders.removeSource(
                repository.getAllRecWithLabelsOrderDateInFolder(
                    true,
                    folder
                )
            )
            displayRecordingsAndFolders.removeSource(
                repository.getAllRecWithLabelsOrderNameInFolder(
                    false,
                    folder
                )
            )
            displayRecordingsAndFolders.removeSource(
                repository.getAllRecWithLabelsOrderDurationInFolder(
                    false,
                    folder
                )
            )
            displayRecordingsAndFolders.removeSource(
                repository.getAllRecWithLabelsOrderDateInFolder(
                    false,
                    folder
                )
            )
            displayRecordingsAndFolders.removeSource(allFolders)
        } else {
            displayRecordingsAndFolders.removeSource(repository.getAllRecordingsWithLabelsOutsideFolder())
            displayRecordingsAndFolders.removeSource(
                repository.getAllRecWithLabelsOrderNameOutsideFolder(
                    true
                )
            )
            displayRecordingsAndFolders.removeSource(
                repository.getAllRecWithLabelsOrderDurationOutsideFolder(
                    true
                )
            )
            displayRecordingsAndFolders.removeSource(
                repository.getAllRecWithLabelsOrderDateOutsideFolder(
                    true
                )
            )
            displayRecordingsAndFolders.removeSource(
                repository.getAllRecWithLabelsOrderNameOutsideFolder(
                    false
                )
            )
            displayRecordingsAndFolders.removeSource(
                repository.getAllRecWithLabelsOrderDurationOutsideFolder(
                    false
                )
            )
            displayRecordingsAndFolders.removeSource(
                repository.getAllRecWithLabelsOrderDateOutsideFolder(
                    false
                )
            )
            displayRecordingsAndFolders.removeSource(allFolders)
        }
    }

    private fun matchLabelsAndRecordings(
        recordingList: List<RecordingAndLabels>,
        params: List<String>
    ): List<RecordingAndLabels>? {
        if (params.isNotEmpty()) {
            val filteredRecordings: ArrayList<RecordingAndLabels> = arrayListOf()
            recordingList.forEach { recording: RecordingAndLabels ->
                filteredRecordings.add(recording)
                if (recording.labels != null) {
                    params.forEach { param: String ->
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
            val refList: MutableList<Pair<Int, MutableList<String>>> = mutableListOf()
            var alreadyInList: Boolean
            reference.forEach { ref: RecordingAndMarkTuple ->
                val pair: Pair<Int, MutableList<String>> =
                    Pair(ref.recordingId, mutableListOf(ref.markerName))
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
        val filteredRef: ArrayList<Int> = arrayListOf()
        val list: List<Pair<Int, MutableList<String>>>? =
            adaptRecordingMarkLiveData(allRecordingsWithMarker.value!!)
        return if (params.isNotEmpty()) {
            list?.forEach { recMarkTuple: Pair<Int, MutableList<String>> ->
                filteredRef.add(recMarkTuple.first)
                params.forEach { param: String ->
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
        var filteredResult: List<Any> = displayRecordingsAndFolders.value!!
        var matchedList: MutableList<RecordingAndLabels> = mutableListOf()
        matchedLabels?.let { filteredResult = matchedLabels }
        matchedMarkers?.let {
            for (rec: Any in filteredResult) {
                if (rec is RecordingAndLabels)
                    if (matchedMarkers.contains(rec.uid)) {
                        matchedList.add(rec)
                    }
            }
            filteredResult = matchedList
        }
        nameInput?.let {
            matchedList = mutableListOf()
            for (rec: Any in filteredResult) {
                if (rec is RecordingAndLabels) {
                    if (rec.recordingName.contains(nameInput, true)) {
                        matchedList.add(rec)
                    }
                }
                filteredResult = matchedList
            }
        }
        @Suppress("UNCHECKED_CAST") return filteredResult as List<RecordingAndLabels>
    }

    fun setFilterResult(labels: List<String>, marks: List<String>, nameInput: String?) {
        removeSortedRecordingSources(currentFolder)
        if (_currentlyInFolder.value!!) {
            val allRecordingsWithLabelsInFolder =
                repository.getAllRecordingsWithLabelsInFolder(currentFolder)
            displayRecordingsAndFolders.addSource(allRecordingsWithLabelsInFolder) {
                if (labels.isNotEmpty() || marks.isNotEmpty() || !nameInput.isNullOrEmpty()) {
                    val matchedLabels =
                        matchLabelsAndRecordings(allRecordingsWithLabelsInFolder.value!!, labels)
                    val matchedMarks = matchMarksAndRecordings(marks)
                    val displayList = combineFilterParams(matchedLabels, matchedMarks, nameInput)
                    displayRecordingsAndFolders.value = displayList
                } else {
                    displayRecordingsAndFolders.value = allRecordingsWithLabelsInFolder.value!!
                }
                _filterEmpty.value = displayRecordingsAndFolders.value!!.isEmpty()
            }
        } else {
            val allRecordingsWithLabelsOutsideFolder =
                repository.getAllRecordingsWithLabelsOutsideFolder()
            displayRecordingsAndFolders.addSource(allRecordingsWithLabelsOutsideFolder) {
                if (labels.isNotEmpty() || marks.isNotEmpty() || !nameInput.isNullOrEmpty()) {
                    val matchedLabels =
                        matchLabelsAndRecordings(
                            allRecordingsWithLabelsOutsideFolder.value!!,
                            labels
                        )
                    val matchedMarks = matchMarksAndRecordings(marks)
                    val displayList = combineFilterParams(matchedLabels, matchedMarks, nameInput)
                    displayRecordingsAndFolders.value = displayList
                } else {
                    displayRecordingsAndFolders.value = allRecordingsWithLabelsOutsideFolder.value!!
                }
                _filterEmpty.value = displayRecordingsAndFolders.value!!.isEmpty()
            }
        }
    }

    fun clearFilter() {
        removeSortedRecordingSources(currentFolder)
        _filterEmpty.value = false
        getRecordingsAndFolders()
    }

    fun setSearchResult(search: String) {
        removeSortedRecordingSources(currentFolder)
        if (_currentlyInFolder.value!!) {
            val recordingsInFolder: LiveData<List<RecordingAndLabels>> =
                repository.getRecordingsByFolder(currentFolder)
            displayRecordingsAndFolders.addSource(recordingsInFolder) {
                val displayList = mutableListOf<RecordingAndLabels>()
                recordingsInFolder.value?.forEach { recording ->
                    if (recording.recordingName.contains(search, true)) {
                        displayList.add(recording)
                    }
                }
                displayRecordingsAndFolders.value = displayList
            }
        } else {
            val recordingsInFolder: LiveData<List<RecordingAndLabels>> =
                repository.getRecordingsOutsideFolder()
            displayRecordingsAndFolders.addSource(recordingsInFolder) {
                val displayList = mutableListOf<RecordingAndLabels>()
                recordingsInFolder.value?.forEach { recording ->
                    if (recording.recordingName.contains(search, true)) {
                        displayList.add(recording)
                    }
                }
                displayRecordingsAndFolders.value = displayList
            }
        }
    }

    fun setSorting(mode: Int?) {
        removeSortedRecordingSources(currentFolder)
        when (mode) {
            R.integer.sort_by_name_asc ->
                if (_currentlyInFolder.value!!) {
                    val allRecordingsWithLabelsOrderNameInFolder: LiveData<List<RecordingAndLabels>> =
                        repository.getAllRecWithLabelsOrderNameInFolder(true, currentFolder)
                    displayRecordingsAndFolders.addSource(allRecordingsWithLabelsOrderNameInFolder) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderNameInFolder, allFolders)
                    }
                    displayRecordingsAndFolders.addSource(allFolders) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderNameInFolder, allFolders)
                    }
                } else {
                    val allRecordingsWithLabelsOrderNameOutsideFolder: LiveData<List<RecordingAndLabels>> =
                        repository.getAllRecWithLabelsOrderNameOutsideFolder(true)
                    displayRecordingsAndFolders.addSource(
                        allRecordingsWithLabelsOrderNameOutsideFolder
                    ) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderNameOutsideFolder, allFolders)
                    }
                    displayRecordingsAndFolders.addSource(allFolders) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderNameOutsideFolder, allFolders)
                    }
                }
            R.integer.sort_by_date_asc -> {
                if (_currentlyInFolder.value!!) {
                    val allRecordingsWithLabelsOrderDateInFolder: LiveData<List<RecordingAndLabels>> =
                        repository.getAllRecWithLabelsOrderDateInFolder(true, currentFolder)
                    displayRecordingsAndFolders.addSource(allRecordingsWithLabelsOrderDateInFolder) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderDateInFolder, allFolders)
                    }
                    displayRecordingsAndFolders.addSource(allFolders) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderDateInFolder, allFolders)
                    }
                } else {
                    val allRecordingsWithLabelsOrderDateOutsideFolder: LiveData<List<RecordingAndLabels>> =
                        repository.getAllRecWithLabelsOrderDateOutsideFolder(true)
                    displayRecordingsAndFolders.addSource(
                        allRecordingsWithLabelsOrderDateOutsideFolder
                    ) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderDateOutsideFolder, allFolders)
                    }
                    displayRecordingsAndFolders.addSource(allFolders) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderDateOutsideFolder, allFolders)
                    }
                }
            }
            R.integer.sort_by_duration_asc -> {
                if (_currentlyInFolder.value!!) {
                    val allRecordingsWithLabelsOrderDurationInFolder: LiveData<List<RecordingAndLabels>> =
                        repository.getAllRecWithLabelsOrderDurationInFolder(true, currentFolder)
                    displayRecordingsAndFolders.addSource(
                        allRecordingsWithLabelsOrderDurationInFolder
                    ) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderDurationInFolder, allFolders)
                    }
                    displayRecordingsAndFolders.addSource(allFolders) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderDurationInFolder, allFolders)
                    }
                } else {
                    val allRecordingsWithLabelsOrderDurationOutsideFolder: LiveData<List<RecordingAndLabels>> =
                        repository.getAllRecWithLabelsOrderDurationOutsideFolder(true)
                    displayRecordingsAndFolders.addSource(
                        allRecordingsWithLabelsOrderDurationOutsideFolder
                    ) {
                        displayRecordingsAndFolders.value =
                            combineData(
                                allRecordingsWithLabelsOrderDurationOutsideFolder,
                                allFolders
                            )
                    }
                    displayRecordingsAndFolders.addSource(allFolders) {
                        displayRecordingsAndFolders.value =
                            combineData(
                                allRecordingsWithLabelsOrderDurationOutsideFolder,
                                allFolders
                            )
                    }
                }
            }
            R.integer.sort_by_name_desc -> {
                if (_currentlyInFolder.value!!) {
                    val allRecordingsWithLabelsOrderNameInFolder: LiveData<List<RecordingAndLabels>> =
                        repository.getAllRecWithLabelsOrderNameInFolder(false, currentFolder)
                    displayRecordingsAndFolders.addSource(allRecordingsWithLabelsOrderNameInFolder) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderNameInFolder, allFolders)
                    }
                    displayRecordingsAndFolders.addSource(allFolders) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderNameInFolder, allFolders)
                    }
                } else {
                    val allRecordingsWithLabelsOrderNameOutsideFolder: LiveData<List<RecordingAndLabels>> =
                        repository.getAllRecWithLabelsOrderNameOutsideFolder(false)
                    displayRecordingsAndFolders.addSource(
                        allRecordingsWithLabelsOrderNameOutsideFolder
                    ) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderNameOutsideFolder, allFolders)
                    }
                    displayRecordingsAndFolders.addSource(allFolders) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderNameOutsideFolder, allFolders)
                    }
                }
            }
            R.integer.sort_by_date_desc -> {
                if (_currentlyInFolder.value!!) {
                    val allRecordingsWithLabelsOrderDateInFolder: LiveData<List<RecordingAndLabels>> =
                        repository.getAllRecWithLabelsOrderDateInFolder(false, currentFolder)
                    displayRecordingsAndFolders.addSource(allRecordingsWithLabelsOrderDateInFolder) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderDateInFolder, allFolders)
                    }
                    displayRecordingsAndFolders.addSource(allFolders) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderDateInFolder, allFolders)
                    }
                } else {
                    val allRecordingsWithLabelsOrderDateOutsideFolder: LiveData<List<RecordingAndLabels>> =
                        repository.getAllRecWithLabelsOrderDateOutsideFolder(false)
                    displayRecordingsAndFolders.addSource(
                        allRecordingsWithLabelsOrderDateOutsideFolder
                    ) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderDateOutsideFolder, allFolders)
                    }
                    displayRecordingsAndFolders.addSource(allFolders) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderDateOutsideFolder, allFolders)
                    }
                }
            }
            R.integer.sort_by_duration_desc -> {
                if (_currentlyInFolder.value!!) {
                    val allRecordingsWithLabelsOrderDurationInFolder: LiveData<List<RecordingAndLabels>> =
                        repository.getAllRecWithLabelsOrderDurationInFolder(false, currentFolder)
                    displayRecordingsAndFolders.addSource(
                        allRecordingsWithLabelsOrderDurationInFolder
                    ) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderDurationInFolder, allFolders)
                    }
                    displayRecordingsAndFolders.addSource(allFolders) {
                        displayRecordingsAndFolders.value =
                            combineData(allRecordingsWithLabelsOrderDurationInFolder, allFolders)
                    }
                } else {
                    val allRecordingsWithLabelsOrderDurationOutsideFolder: LiveData<List<RecordingAndLabels>> =
                        repository.getAllRecWithLabelsOrderDurationOutsideFolder(false)
                    displayRecordingsAndFolders.addSource(
                        allRecordingsWithLabelsOrderDurationOutsideFolder
                    ) {
                        displayRecordingsAndFolders.value =
                            combineData(
                                allRecordingsWithLabelsOrderDurationOutsideFolder,
                                allFolders
                            )
                    }
                    displayRecordingsAndFolders.addSource(allFolders) {
                        displayRecordingsAndFolders.value =
                            combineData(
                                allRecordingsWithLabelsOrderDurationOutsideFolder,
                                allFolders
                            )
                    }
                }
            }

            else -> {
                getRecordingsAndFolders()
            }
        }
    }

    private fun getRecordingsAndFolders() {
        if (_currentlyInFolder.value!!) {
            val allRecordingsWithLabelsInFolder: LiveData<List<RecordingAndLabels>> =
                repository.getAllRecordingsWithLabelsInFolder(currentFolder)
            displayRecordingsAndFolders.addSource(allRecordingsWithLabelsInFolder) {
                displayRecordingsAndFolders.value =
                    combineData(allRecordingsWithLabelsInFolder, allFolders)
            }
            displayRecordingsAndFolders.addSource(allFolders) {
                displayRecordingsAndFolders.value =
                    combineData(allRecordingsWithLabelsInFolder, allFolders)
            }
        } else {
            val allRecordingsWithLabelsOutsideFolder: LiveData<List<RecordingAndLabels>> =
                repository.getAllRecordingsWithLabelsOutsideFolder()
            displayRecordingsAndFolders.addSource(allRecordingsWithLabelsOutsideFolder) {
                displayRecordingsAndFolders.value =
                    combineData(allRecordingsWithLabelsOutsideFolder, allFolders)
            }
            displayRecordingsAndFolders.addSource(allFolders) {
                displayRecordingsAndFolders.value =
                    combineData(allRecordingsWithLabelsOutsideFolder, allFolders)
            }
        }
    }

    private fun combineData(
        recordingsData: LiveData<List<RecordingAndLabels>>,
        folders: LiveData<List<FolderEntity>>
    ): List<Any> {
        val all = ArrayList<Any>()
        val recordings: List<RecordingAndLabels>? = recordingsData.value
        if (recordings != null) {
            for (rec: RecordingAndLabels in recordings) {
                all.add(rec)
            }
        }
        if (folders.value != null && !_currentlyInFolder.value!!) all.addAll(folders.value!!)
        return all
    }

    fun openCreateFolderDialog() {
        _folderDialog.value = true
    }

    fun openFolderMenu(folder: FolderEntity, view: View) = PopupMenu(view.context, view).run {
        menuInflater.inflate(R.menu.popup_menu_folder, menu)
        setOnMenuItemClickListener { item: MenuItem ->
            when (item.toString()) {
                context.getString(R.string.folder_rename_folder) -> {
                    folderToBeEdited = folder
                    _folderDialog.value = true
                }
                context.getString(R.string.folder_delete_folder) -> {
                    folderToBeEdited = folder
                    deleteFolder = true
                    _folderDialog.value = true
                }
            }
            true
        }
        show()
    }

    fun resetValues() {
        _folderDialog.value = false
        folderToBeEdited = null
        errorMessage = null
        deleteFolder = false
    }

    fun renameFolder(folder: FolderEntity, newName: String) {
        resetValues()
        if (newName.isEmpty()) {
            errorMessage = context.getString(R.string.folder_dialog_error_no_name)
            _folderDialog.value = true
            return
        }
        repository.updateFolder(FolderEntity(folder.uid, newName))
    }

    fun createFolder(folderName: String) {
        resetValues()
        if (folderName.isEmpty()) {
            errorMessage = context.getString(R.string.folder_dialog_error_no_name)
            _folderDialog.value = true
            return
        }
        repository.insertFolder(FolderEntity(0, folderName))
    }

    fun deleteFolder(folder: FolderEntity) {
        resetValues()
        deleteRecordings.addSource(repository.getRecordingsByFolder(folder.uid)) { list: List<RecordingAndLabels> ->
            list.forEach {
                File(it.recordingPath).delete()
                repository.deleteFolderAssignment(it)
                repository.deleteRecMarks(it.uid)
                repository.deleteRecLabels(it.uid)
                repository.deleteRecording(it.uid)
            }
        }
        repository.deleteFolder(folder)
    }

    fun moveRecording(recordingId: Int, destinationFolder: FolderEntity) {
        val folderAssignmentEntity = FolderAssignmentEntity(0, recordingId, destinationFolder.uid)
        repository.insertFolderAssignment(folderAssignmentEntity)
        repository.updateFolder(FolderEntity(destinationFolder.uid, destinationFolder.folderName))
    }

    fun removeRecordingFromFolder(recordingAndLabels: RecordingAndLabels) {
        repository.deleteFolderAssignment(recordingAndLabels)
        recordingToBeMoved = null
    }

    fun onFolderClicked(folder: FolderEntity) {
        removeSortedRecordingSources(currentFolder)
        _currentlyInFolder.value = true
        currentFolder = folder.uid
        val recordingsInFolder: LiveData<List<RecordingAndLabels>> =
            repository.getRecordingsByFolder(folder.uid)
        displayRecordingsAndFolders.addSource(recordingsInFolder) {
            displayRecordingsAndFolders.value = it
        }
    }

    fun onLeaveFolderClicked() {
        removeSortedRecordingSources(currentFolder)
        _currentlyInFolder.value = false
        currentFolder = 0
        if (_currentlyInFolder.value!!) {
            val allRecordingsWithLabelsInFolder: LiveData<List<RecordingAndLabels>> =
                repository.getAllRecordingsWithLabelsInFolder(currentFolder)
            displayRecordingsAndFolders.addSource(allRecordingsWithLabelsInFolder) {
                displayRecordingsAndFolders.value =
                    combineData(allRecordingsWithLabelsInFolder, allFolders)
            }
            displayRecordingsAndFolders.addSource(allFolders) {
                displayRecordingsAndFolders.value =
                    combineData(allRecordingsWithLabelsInFolder, allFolders)
            }
        } else {
            val allRecordingsWithLabelsOutsideFolder: LiveData<List<RecordingAndLabels>> =
                repository.getAllRecordingsWithLabelsOutsideFolder()
            displayRecordingsAndFolders.addSource(allRecordingsWithLabelsOutsideFolder) {
                displayRecordingsAndFolders.value =
                    combineData(allRecordingsWithLabelsOutsideFolder, allFolders)
            }
            displayRecordingsAndFolders.addSource(allFolders) {
                displayRecordingsAndFolders.value =
                    combineData(allRecordingsWithLabelsOutsideFolder, allFolders)
            }
        }
    }

    private val _navigateToPlayerFragment = MutableLiveData<MutableList<String>>()
    val navigateToPlayerFragment: MutableLiveData<MutableList<String>>
        get() = _navigateToPlayerFragment

    fun onRecordingClicked(recordingId: Int, recordingName: String, recordingPath: String) {
        val recording: MutableList<String> = mutableListOf()
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
