package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.lifecycle.*
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
    private val res = context.resources
    private lateinit var frameLayout: FrameLayout
    var recordingToBeExported: EntryEntity? = null
    var recordingToBeMoved: EntryEntity? = null

    val allRecordings: LiveData<List<EntryEntity>> = repository.getAllRecordings()
    val allRecordingsWithNoFolder: LiveData<List<EntryEntity>> = repository.getRecordingWithNoFolder()
    val allRecNoFolderSortByName: LiveData<List<EntryEntity>> = repository.getRecNoFolderSortByName()
    val allRecNoFolderSortByDate: LiveData<List<EntryEntity>> = repository.getRecNoFolderSortByDate()
    val allRecNoFolderSortByDur: LiveData<List<EntryEntity>> = repository.getRecNoFolderSortByDur()


    var errorMessage: String? = null
    var recording: EntryEntity? = null

    var sortedFolderContent = MediatorLiveData<List<EntryEntity>>()
    var displayRecordings = MediatorLiveData<List<EntryEntity>>()
    var sortByListener:  MutableLiveData<Int> = MutableLiveData()

    private val _createSearchDialog = MutableLiveData<Boolean>()
    val createSearchDialog: MutableLiveData<Boolean>
        get() = _createSearchDialog

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

    fun initListener(){
        sortByListener.value = res.getInteger(R.integer.sort_by_date)
    }

    fun onSearchClicked(){
        println("onSearchClicked")
        createSearchDialog.value = true
    }

    fun cancelSearchDialog(){
        createSearchDialog.value = false
    }

    fun searchRecording(searchInput: String){
        println("ES WIRT GESUCHT ")
        println(searchInput)
    }

    fun setRecordingDisplay (){
        when (sortByListener.value){
            res.getInteger(R.integer.sort_by_date) -> {
                displayRecordings.removeSource(allRecNoFolderSortByName)
                displayRecordings.removeSource(allRecNoFolderSortByDur)
                displayRecordings.addSource(allRecNoFolderSortByDate){
                    displayRecordings.value = allRecNoFolderSortByDate.value
                }
            }
            res.getInteger(R.integer.sort_by_name) -> {
                displayRecordings.removeSource(allRecNoFolderSortByDate)
                displayRecordings.removeSource(allRecNoFolderSortByDur)
                displayRecordings.addSource(allRecNoFolderSortByName) {
                    displayRecordings.value = allRecNoFolderSortByName.value
                }
            }
            res.getInteger(R.integer.sort_by_duration) -> {
                displayRecordings.removeSource(allRecNoFolderSortByName)
                displayRecordings.removeSource(allRecNoFolderSortByDate)
                displayRecordings.addSource(allRecNoFolderSortByDur) {
                    displayRecordings.value = allRecNoFolderSortByDur.value
                }
            }
        }
    }

    fun openSortByPopupMenu(view: View){
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu_sort_by, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId){
                R.id.action_sort_by_date ->
                {sortByListener.value = res.getInteger(R.integer.sort_by_date)
                    setRecordingDisplay()
                }

                R.id.action_sort_by_name ->
                {sortByListener.value = res.getInteger(R.integer.sort_by_name)
                    setRecordingDisplay()
                }

                R.id.action_sort_by_duration ->
                { sortByListener.value = res.getInteger(R.integer.sort_by_duration)
                    setRecordingDisplay()
                }
            }
            true
        }
        popupMenu.show()
    }

    fun getAllRecordingsByFolder(folder : FolderEntity): LiveData<List<EntryEntity>>{
        val allRecFolderSortByName: LiveData<List<EntryEntity>> = repository.getRecByFolderSortedName(folder.uid)
        val allRecFolderSortByDate: LiveData<List<EntryEntity>> = repository.getRecByFolderSortedDate(folder.uid)
        val allRecFolderSortByDur: LiveData<List<EntryEntity>> = repository.getRecByFolderSortedDuration(folder.uid)

        when(sortByListener.value){
            res.getInteger(R.integer.sort_by_date) ->{
                sortedFolderContent.removeSource(allRecFolderSortByDur)
                sortedFolderContent.removeSource(allRecFolderSortByName)
                sortedFolderContent.addSource(allRecFolderSortByDate){
                    sortedFolderContent.value = allRecFolderSortByDate.value
                }
            }
            res.getInteger(R.integer.sort_by_name) ->{
                sortedFolderContent.removeSource(allRecFolderSortByDur)
                sortedFolderContent.removeSource(allRecFolderSortByDate)
                sortedFolderContent.addSource(allRecFolderSortByName){
                    sortedFolderContent.value = allRecFolderSortByName.value
                }
            }
            res.getInteger(R.integer.sort_by_duration) ->{
                sortedFolderContent.removeSource(allRecFolderSortByDate)
                sortedFolderContent.removeSource(allRecFolderSortByName)
                sortedFolderContent.addSource(allRecFolderSortByDur){
                    sortedFolderContent.value = allRecFolderSortByDur.value
                }
            }
        }
        return sortedFolderContent
    }

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

    fun delete(entryEntity: EntryEntity) {
        recording = entryEntity
        _createConfirmDialog.value = true
    }

    fun deleteRecording(entryEntity: EntryEntity) {
        val deletedSuccessful = StorageHelper.deleteFile(context, entryEntity)
        if (deletedSuccessful) {
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

    fun deleteEntriesInFolders(folderRefs: List<Int>) {
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
