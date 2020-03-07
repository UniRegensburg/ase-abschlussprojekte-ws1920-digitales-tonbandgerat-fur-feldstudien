package de.ur.mi.audidroid.viewmodels

import android.app.Application
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.material.snackbar.Snackbar
import de.ur.mi.audidroid.R
import de.ur.mi.audidroid.models.EntryEntity
import de.ur.mi.audidroid.models.Repository
import de.ur.mi.audidroid.utils.ShareHelper
import de.ur.mi.audidroid.views.MainActivity
import java.io.File

/**
 * ViewModel for FilesFragment.
 * @author: Theresa Strohmeier
 */
class FilesViewModel(dataSource: Repository, application: Application) :
    AndroidViewModel(application) {

    private val repository = dataSource
    private val context = getApplication<Application>().applicationContext
    private val _createAlertDialog = MutableLiveData<Boolean>()
    val allRecordings: LiveData<List<EntryEntity>> = repository.getAllRecordings()
    private lateinit var frameLayout: FrameLayout
    var errorMessage: String? = null
    var recording: EntryEntity? = null
    private var recordingToBeExported: EntryEntity? = null

    private val _createConfirmDialog = MutableLiveData<Boolean>()
    private var _showSnackbarEvent = MutableLiveData<Boolean>()

    val createConfirmDialog: LiveData<Boolean>
        get() = _createConfirmDialog

    val createAlertDialog: MutableLiveData<Boolean>
        get() = _createAlertDialog

    val showSnackbarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = null
    }

    // If there are no recordings in the database, a TextView is displayed.
    val empty: LiveData<Boolean> = Transformations.map(allRecordings) {
        it.isEmpty()
    }

    // When the ImageButton is clicked, a PopupMenu opens.
    fun onButtonClicked(entryEntity: EntryEntity, view: View) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete_recording ->
                    delete(entryEntity)
                R.id.action_share_recording -> {
                    recordingToBeExported = entryEntity
                    _createAlertDialog.value = true
                }
            }
            true
        }
        popupMenu.show()
    }

    fun shareRecording(format: String) {
        ShareHelper.shareAudio(recordingToBeExported!!, format, context)
        _createAlertDialog.value = false
    }

    private fun delete(entryEntity: EntryEntity) {
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
            }
        }
        if (array.size == 0 && it.isNotEmpty()) {
            repository.deleteAllRecordings()
        }
        return array
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
